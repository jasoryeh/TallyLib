package tk.jasonho.tally.api.models;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.util.Constants;

import java.text.ParseException;
import java.time.Instant;

public class GamePlayer {
    @Getter private final int id;
    @Getter private Game game;
    @Getter private String gameCharacterID;
    @Getter private JsonObject extras; // primitives only? this is technically defaulting as a json object
    @Getter private Instant updatedAt;
    @Getter private final Instant createdAt;

    public static GamePlayer deserialize(JsonObject obj, TallyStatsManager manager) throws ParseException {
        return new GamePlayer(obj, manager);
    }

    private GamePlayer(JsonObject obj, TallyStatsManager manager) throws ParseException  {
        this(obj.get("id").getAsInt(), manager.access().getGame(obj.get("game").getAsInt()),
                obj.get("gameCharacterID").getAsString(),
                new JsonParser().parse(obj.get("extras").getAsString()).getAsJsonObject(),
                Constants.JS_DEFAULT_FORMAT.parse(obj.get("updatedAt").getAsString()).toInstant(),
                Constants.JS_DEFAULT_FORMAT.parse(obj.get("createdAt").getAsString()).toInstant());
    }

    private GamePlayer(int id, Game game, String gameCharacterID, JsonObject extras,
                       Instant updatedAt, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.set(game, gameCharacterID, extras, updatedAt);
    }

    private void set(Game game, String gameCharacterID, JsonObject extras, Instant updatedAt) {
        this.game = game;
        this.gameCharacterID = gameCharacterID;
        this.extras = extras;
        this.updatedAt = updatedAt;
    }

    private void set(JsonObject obj, TallyStatsManager manager) throws ParseException  {
        this.set(manager.access().getGame(obj.get("game").getAsInt()),
                obj.get("gameCharacterID").getAsString(),
                new JsonParser().parse(obj.get("extras").getAsString()).getAsJsonObject(),
                Constants.JS_DEFAULT_FORMAT.parse(obj.get("updatedAt").getAsString()).toInstant());
    }

    public void update(TallyStatsManager manager) throws ParseException  {
        this.set(manager.access().raw_getGamePlayer(this.game, this.gameCharacterID), manager);
    }
}
