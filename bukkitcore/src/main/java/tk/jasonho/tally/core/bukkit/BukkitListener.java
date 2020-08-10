package tk.jasonho.tally.core.bukkit;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;

import java.util.stream.Collectors;

/**
 * Listens for generic events for analytical purposes
 *
 * Logins,
 * Logouts,
 * Kicks,
 * Chat,
 * Commands
 */
public class BukkitListener extends TallyListener {

    public BukkitListener(TallyOperationHandler operationHandler) {
        super(operationHandler);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        JsonObject jsonObject = BukkitUtils.playerToData(player);
        jsonObject.addProperty("connected_via", event.getHostname());

        super.operationHandler.track("login", null, player.getUniqueId(), jsonObject);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        JsonObject jsonObject = BukkitUtils.playerToData(player);

        super.operationHandler.track("join", null, player.getUniqueId(), jsonObject);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        JsonObject jsonObject = BukkitUtils.playerToData(player);
        jsonObject.addProperty("quit_message", event.getQuitMessage());

        super.operationHandler.track("logout", null, player.getUniqueId(), jsonObject);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        JsonObject jsonObject = BukkitUtils.playerToData(player);
        jsonObject.addProperty("leave_message", event.getLeaveMessage());
        jsonObject.addProperty("reason", event.getReason());

        super.operationHandler.track("kick", null, player.getUniqueId(), jsonObject);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        JsonObject jsonObject = BukkitUtils.playerToData(player);
        jsonObject.addProperty("cancelled", event.isCancelled());
        if(this.operationHandler.getTally().getConfig().getBoolean("chat", false)) {
            jsonObject.addProperty("message", event.getMessage());
            jsonObject.addProperty("format", event.getFormat());
        } else {
            jsonObject.add("message", JsonNull.INSTANCE); // messages/commands sent are hidden by default
            jsonObject.add("format", JsonNull.INSTANCE); // messages/commands sent are hidden by default
        }
        jsonObject.addProperty("recipients", event.getRecipients().stream().map(p -> p.getUniqueId().toString()).collect(Collectors.joining(", ")));
        jsonObject.addProperty("world", event.getWorld().getName());

        super.operationHandler.track("chat", null, player.getUniqueId(), jsonObject);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        JsonObject jsonObject = BukkitUtils.playerToData(player);
        jsonObject.addProperty("cancelled", event.isCancelled());
        if (this.operationHandler.getTally().getConfig().getBoolean("command", false)) {
            jsonObject.addProperty("command", event.getMessage());
        } else {
            jsonObject.add("command", JsonNull.INSTANCE); // messages/commands sent are hidden by default
        }
        jsonObject.addProperty("world", event.getWorld().getName());

        super.operationHandler.track("chat", null, player.getUniqueId(), jsonObject);
    }

}
