package tk.jasonho.tally.core.bukkit;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.UUID;

/**
 * Listens for normal PVP combat events (kills, deaths only) and stores them
 *
 * Can be unregistered, if a better implementation exists
 */
public class BukkitCombatListener extends TallyListener {

    public BukkitCombatListener(TallyOperationHandler operationHandler) {
        super(operationHandler);
    }

    public Entity resolve(Entity damager) {
        if(!(damager instanceof Player)) {
            if(damager instanceof Projectile) {
                Projectile projectile = ((Projectile) damager);
                ProjectileSource shooter = projectile.getShooter();
                return shooter instanceof Player ? ((Player) shooter) : null;
            } else {
                return null; // todo: unknowns.
            }
        } else {
            return ((Player) damager);
        }
    }

    /**
     * This is the listener to store default Bukkit deaths.
     * If your plugin has a more specialized/better implementation of this
     * or you want to add more information to this pvp tracker,
     * feel free to unregister this event listener via
     * TallyPlugin.getInstance().unregisterTallyListener.
     *
     * @param event from bukkit
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if(!(entity instanceof Player)) {
            return; // not player
        }
        if( (((Player) entity).getHealth() - event.getFinalDamage()) >= 0.0 ) {
            // health >= 0 = alive
            return;
        }

        if(event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event1 = (EntityDamageByEntityEvent) event;
            this.handleDeath(((Player) entity), this.resolve(event1.getActor()));
        } else {
            this.handleDeath(((Player) entity), null);
        }
    }

    public void handleDeath(Player player, Entity cause) {
        UUID player1 = player.getUniqueId();
        UUID causer1 = !(cause instanceof Player) ? DamageTrackModule.ENVIRONMENT : ((Player) cause).getUniqueId();

        this.operationHandler.track("KILL", player1, causer1); // causer1 killed player1
        this.operationHandler.track("DEATH", causer1, player1); // player1 death from causer1
    }

}
