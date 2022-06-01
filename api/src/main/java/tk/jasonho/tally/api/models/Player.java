package tk.jasonho.tally.api.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.helpers.MapsTo;
import tk.jasonho.tally.api.models.helpers.Model;

@Data
public class Player extends Model {
    @MapsTo("id")
    private Integer id;
    @MapsTo("game")
    private Integer game;
    @MapsTo("identifier")
    private String identifier;
    @MapsTo("belongsTo")
    private Integer belongsTo;

    public Player() {}

    @SneakyThrows
    public void save(TallyStatsManager manager) {
        JsonObject instance = manager.connectionBuilder("player")
                .post()
                .writeJson(Model.serialize(this))
                .verifyJsonThrowing()
                .getReadJson()
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject();
        this.updateWith(Model.deserialize(Player.class, instance));
    }

    @SneakyThrows
    public static Player of(TallyStatsManager manager, Game game, String identifier) {
        JsonElement data = manager.connectionBuilder("player/query?game=" + game.getId() + "&identifier=" + identifier)
                .get()
                .getReadJson()
                .getAsJsonObject()
                .get("data");
        Player player;
        if (data.isJsonNull()) {
            player = new Player();
            player.setGame(game.getId());
            player.setIdentifier(identifier);
            player.save(manager);
        } else {
            player = Model.deserialize(Player.class, data.getAsJsonObject());
        }
        return player;
    }
}
