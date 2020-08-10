package tk.jasonho.tally.api.models;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.util.Constants;

import java.text.ParseException;
import java.time.Instant;

public class Game {

    @Getter private final int id;
    @Getter private String tag;
    @Getter private String name;
    @Getter private String description;
    @Getter private boolean enabled;
    @Getter private Instant updatedAt;
    @Getter private final Instant createdAt;

    public static Game deserialize(JsonObject obj) throws ParseException {
        return new Game(obj);
    }

    private Game(JsonObject obj) throws ParseException  {
        this(obj.get("id").getAsInt(), obj.get("tag").isJsonNull() ? null : obj.get("tag").getAsString(),
                obj.get("name").getAsString(), obj.get("description").getAsString(), obj.get("enabled").getAsBoolean(),
                Constants.JS_DEFAULT_FORMAT.parse(obj.get("updatedAt").getAsString()).toInstant(),
                Constants.JS_DEFAULT_FORMAT.parse(obj.get("createdAt").getAsString()).toInstant());
    }

    private Game(int id, String tag, String name, String description,
                boolean enabled, Instant updatedAt, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.set(tag, name, description, enabled, updatedAt);
    }

    private void set(String tag, String name, String description,
                     boolean enabled, Instant updatedAt) {
        this.tag = tag;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.updatedAt = updatedAt;
    }

    private void set(JsonObject obj) throws ParseException  {
        this.set(obj.get("tag").isJsonNull() ? null : obj.get("tag").getAsString(),
                obj.get("name").getAsString(), obj.get("description").getAsString(), obj.get("enabled").getAsBoolean(),
                Constants.JS_DEFAULT_FORMAT.parse(obj.get("updatedAt").getAsString()).toInstant());
    }

    public void update(TallyStatsManager manager) throws ParseException  {
        this.set(manager.access().raw_getGame(this.id));
    }

}
