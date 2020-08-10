package tk.jasonho.tally.api.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import tk.jasonho.tally.api.util.Constants;
import tk.jasonho.tally.api.TallyStatsManager;

import java.text.ParseException;
import java.time.Instant;

public class Statistic {

    @Getter
    private String objective;
    @Getter
    private Game game;
    @Getter
    private GamePlayer actor;
    @Getter
    private GamePlayer receiver;
    @Getter
    private boolean hidden;
    @Getter
    private JsonArray labels;
    @Getter
    public long id;
    @Getter public Instant updatedAt;
    @Getter public final Instant createdAt;

    public static Statistic deserialize(JsonObject obj, TallyStatsManager manager) throws ParseException {
        return new Statistic(obj, manager);
    }

    private Statistic(JsonObject obj, TallyStatsManager manager) throws ParseException {
        this(obj.get("id").getAsLong(), obj.get("type").getAsString(),
                manager.access().getGame(obj.get("gameType").getAsInt()),
                obj.get("hide").getAsBoolean(),
                new JsonParser().parse(obj.get("labels").getAsString()).getAsJsonArray(),
                Constants.JS_DEFAULT_FORMAT.parse(obj.get("updatedAt").getAsString()).toInstant(),
                Constants.JS_DEFAULT_FORMAT.parse(obj.get("createdAt").getAsString()).toInstant(),
                obj, manager);
    }

    private Statistic(long id, String objective, Game game, boolean hidden, JsonArray labels,
                     Instant updatedAt, Instant createdAt, JsonObject obj, TallyStatsManager manager) {
        this(id, objective, game, manager.access().getGamePlayer(game, obj.get("actor").getAsString()),
                manager.access().getGamePlayer(game, obj.get("receiver").getAsString()),
                hidden, labels, updatedAt, createdAt);
    }

    private Statistic(long id, String objective, Game game, GamePlayer actor, GamePlayer receiver, boolean hidden,
                     JsonArray labels, Instant updatedAt, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.set(objective, game, actor, receiver, hidden, labels, updatedAt);
    }

    public void set(String objective, Game game, GamePlayer actor, GamePlayer receiver, boolean hidden,
                    JsonArray labels, Instant updatedAt) {
        this.objective = objective;
        this.game = game;
        this.actor = actor;
        this.receiver = receiver;
        this.hidden = hidden;
        this.labels = labels;
        this.updatedAt = updatedAt;
    }

    private void set(JsonObject obj, TallyStatsManager manager) throws ParseException {
        Game gameType = manager.access().getGame(obj.get("gameType").getAsInt());
        this.set(obj.get("type").getAsString(), gameType,
                manager.access().getGamePlayer(game, obj.get("actor").getAsString()),
                manager.access().getGamePlayer(game, obj.get("receiver").getAsString()),
                obj.get("hide").getAsBoolean(),
                new JsonParser().parse(obj.get("labels").getAsString()).getAsJsonArray(),
                Constants.JS_DEFAULT_FORMAT.parse(obj.get("updatedAt").getAsString()).toInstant());
    }

    public void update(TallyStatsManager manager) throws ParseException  {
        this.set(manager.access().raw_getStatistic(this.getId()), manager);
    }


}
