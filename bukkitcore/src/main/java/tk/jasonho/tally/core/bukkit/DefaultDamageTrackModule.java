package tk.jasonho.tally.core.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

public class DefaultDamageTrackModule extends DamageTrackModule {

    public DefaultDamageTrackModule(TallyOperationHandler operationHandler, String id) {
        super(operationHandler, id);
    }

    /**
     * Tracks damages taken and given
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) { return; } // not a player getting damage
        final UUID defender = ((Player) event.getEntity()).getUniqueId();
        final double damage = event.getFinalDamage();
        final UUID attacker;

        if(event instanceof EntityDamageByEntityEvent) {
            // Non-player damage entity
            EntityDamageByEntityEvent damageEvent = ((EntityDamageByEntityEvent) event);

            // If either the entity getting damaged or damage dealer is not a player
            if(!(damageEvent.getDamager() instanceof Player)) {
                // but it is a projectile (snowball, arrow, etc.) replace attacker with the shooter of the projectile
                if(damageEvent.getDamager() instanceof Projectile) {
                    Projectile projectile = ((Projectile) damageEvent.getDamager());
                    attacker = projectile.getShooter() instanceof Player ?
                            ((Player) projectile.getShooter()).getUniqueId() : // player
                            ENVIRONMENT; // todo: assign uuids to entity types
                } else {
                    // not a projectile, don't track random non-player entity to entity damage
                    TallyPlugin.getInstance().optionalLog("Untracked e<->e damage: " + damageEvent.getCause());
                    // todo: unknown?
                    attacker = ENVIRONMENT;
                }
            } else {
                // player
                attacker = ((Player) damageEvent.getDamager()).getUniqueId();
            }
        } else {
            // Non entity to entity damage event (natural? ex. falling?)
            // environmental damage
            attacker = ENVIRONMENT;
        }

        // Save the information
        super.onDamage(
                attacker,
                defender,
                damage,
                DamageDirection.GIVE);
    }

    /**
     * Recap player on death of the damages they've exchanged with other things
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void showLifeRecap(PlayerDeathEvent event) {
        // Display stats then recap
        if(event.getEntity() != null) {
            super.onDeath(event.getEntity());
        }
    }

    /**
     * Reset lifetime tracked damage exchanges when player respawns
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void trackUntracked(PlayerRespawnEvent event) {
        if(event.getPlayer() != null) {
            TallyPlugin.getInstance().getDamageTrackModule()
                    .trackUntracked(event.getPlayer().getUniqueId());
        }
    }

}
