package tk.jasonho.tally.api.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.helpers.MapsTo;
import tk.jasonho.tally.api.models.helpers.Model;

import java.util.ArrayList;
import java.util.List;

@Data
public class Game extends Model {
    @MapsTo("id")
    private Integer id;
    @MapsTo("tag")
    private String tag;
    @MapsTo("name")
    private String name;
    @MapsTo("description")
    private String description;

    public Game() {}

    @SneakyThrows
    public static List<Game> all(TallyStatsManager manager) {
        ArrayList<Game> games = new ArrayList<>();
        for (JsonElement data : manager.connectionBuilder("game/list")
                .get()
                .getReadJson()
                .getAsJsonObject()
                .get("data")
                .getAsJsonArray()) {
            games.add(Model.deserialize(Game.class, data.getAsJsonObject()));
        }
        return games;
    }

    @SneakyThrows
    public static Game ofTag(TallyStatsManager manager, String tag) {
        JsonObject data = manager.connectionBuilder("game/tag?tag=" + tag)
                .get()
                .getReadJson()
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject();
        return Model.deserialize(Game.class, data);
    }
}
