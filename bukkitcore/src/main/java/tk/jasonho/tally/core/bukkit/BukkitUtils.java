package tk.jasonho.tally.core.bukkit;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitUtils {
    public static int getProtocol(Player player) {
        try {
            Class<?> aClass = Class.forName("us.myles.ViaVersion.api.Via");
            if(aClass != null) {
                Object api = aClass.getDeclaredMethod("getAPI").invoke(null);

                api.getClass()
                        .getDeclaredMethod("getPlayerVersion", UUID.class)
                        .invoke(api, player.getUniqueId());

                us.myles.ViaVersion.api.Via.getAPI().getPlayerVersion(player.getUniqueId());
            } else {
                TallyPlugin.getInstance().getLogger().warning("ViaVersion could not be accessed.");
            }
        } catch(Exception ex) {
            // no viaversion/failed
        }
        return player.getProtocolVersion();
    }

    public static JsonObject playerToData(Player player) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", player.getName());
        jsonObject.addProperty("protocol", getProtocol(player));
        if(player.getAddress() != null) {
            jsonObject.addProperty("ip", player.getAddress()
                    .getAddress()
                    .getHostAddress());
        }
        return jsonObject;
    }
}
