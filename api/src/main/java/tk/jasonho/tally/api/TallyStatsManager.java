package tk.jasonho.tally.api;

import com.google.gson.*;
import tk.jasonho.tally.api.interfacing.TallyConnectionBuilder;
import tk.jasonho.tally.api.interfacing.TallyStatsVersion;
import tk.jasonho.tally.api.models.Game;
import tk.jasonho.tally.api.models.GamePlayer;
import tk.jasonho.tally.api.models.Statistic;
import tk.jasonho.tally.api.util.TallyLogger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class TallyStatsManager {
    private final TallyConfiguration configuration;
    private final TallyStatsVersion version;
    private final DataAccessor accessor;

    public TallyStatsManager(TallyConfiguration configuration, TallyStatsVersion version) {
        this.configuration = configuration;
        this.version = version;
        this.accessor = new DataAccessor(this);

        this.test();
    }

    protected String getEndpoint(String sub) {
        return this.configuration.getHost() + "api/" + version.toString().toLowerCase() + "/" + sub;
    }

    protected TallyConnectionBuilder connectionBuilder(String sub) {
        return new TallyConnectionBuilder(this.getEndpoint(sub)).authBearer(this.configuration.getAuth());
    }

    public boolean test() {
        return this.connectionBuilder("")
                .get()
                .readIn()
                .verifyJson(true);
    }

    public DataAccessor access() {
        return this.accessor;
    }

    /**
     * Holds all the data retrievers
     */
    public static class DataAccessor {
        private final TallyStatsManager manager;
        public DataAccessor(TallyStatsManager manager) {
            this.manager = manager;
        }

        /**
         * Supported games
         */

        public JsonObject raw_getGames() {
            JsonElement games = this.manager
                    .connectionBuilder("games")
                    .get()
                    .readIn()
                    .verifyJsonReturn(true);
            return games != null ? games.getAsJsonObject() : null;
        }

        public JsonObject raw_getGame(int gameId) {
            JsonObject jsonObject = this.raw_getGames();
            if(jsonObject == null || !jsonObject.has("data") || jsonObject.get("data").isJsonNull()) {
                return null;
            } else {
                JsonArray data = jsonObject.get("data")
                        .getAsJsonArray();
                for (JsonElement datum : data) {
                    if(datum.isJsonObject()) {
                        JsonObject objectInArray = datum.getAsJsonObject();
                        if(!objectInArray.has("id") || objectInArray.get("id").isJsonNull()
                                || objectInArray.get("id").getAsInt() != gameId) {
                            continue;
                        } else {
                            return objectInArray;
                        }
                    }
                }
                return null;
            }
        }

        public JsonObject raw_getGame(String tag) {
            JsonObject jsonObject = this.raw_getGames();
            if(jsonObject == null || !jsonObject.has("data") || jsonObject.get("data") == null) {
                return null;
            } else {
                JsonArray data = jsonObject.get("data")
                        .getAsJsonArray();
                for (JsonElement datum : data) {
                    if(datum.isJsonObject()) {
                        JsonObject objectInArray = datum.getAsJsonObject();
                        if(!objectInArray.has("tag") || objectInArray.get("tag").isJsonNull()
                                || !objectInArray.get("tag").getAsString().equalsIgnoreCase(tag)) {
                            continue;
                        } else {
                            return objectInArray;
                        }
                    }
                }
                return null;
            }
        }

        public Game getGame(int id) {
            try {
                return Game.deserialize(this.raw_getGame(id));
            } catch(ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        public Game getGame(String tag) {
            try {
                return Game.deserialize(this.raw_getGame(tag));
            } catch(ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Players
         */
        public JsonObject raw_getGamePlayer(Game game, String gameUserID) {
            JsonElement response = this.manager
                    .connectionBuilder("player?gameId=" + game.getId() + "&id=" + gameUserID)
                    .get()
                    .readIn()
                    .verifyJsonReturn(true);
            if(response == null) {
                return null;
            }

            JsonObject jsonObject = response.getAsJsonObject();
            if(jsonObject == null || !jsonObject.has("data") || jsonObject.get("data") == null) {
                return null;
            } else {
                JsonArray data = jsonObject.get("data").getAsJsonArray();
                for (JsonElement datum : data) {
                    if(datum.isJsonObject()) {
                        JsonObject objectInArray = datum.getAsJsonObject();
                        if(!objectInArray.get("gameCharacterID").equals(gameUserID)) {
                            continue;
                        } else {
                            return objectInArray;
                        }
                    }
                }
                return null;
            }
        }

        public GamePlayer getGamePlayer(Game game, String gameUserID) {
            JsonObject jsonObject = this.raw_getGamePlayer(game, gameUserID);
            try {
                return GamePlayer.deserialize(jsonObject, this.manager);
            } catch(ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         *
         * Statistics
         *
         */

        public JsonObject raw_getStatistic(long id) {
            JsonElement jsonElement = this.manager
                    .connectionBuilder("statistic?id=" + id)
                    .get()
                    .readIn()
                    .verifyJsonReturn(true);
            if(jsonElement == null) {
                return null;
            } else {
                return jsonElement.getAsJsonObject();
            }
        }

        public Statistic getStatistic(long id) {
            try {
                return Statistic.deserialize(this.raw_getStatistic(id), this.manager);
            } catch(ParseException e) {
                return null;
            }
        }

        public long addStatistic(String objective, Game game, String actor, String recvr) {
            return this.addStatistic(objective, game, actor, recvr, false);
        }

        public long addStatistic(String objective, Game game, String actor, String recvr, boolean hidden) {
            return this.addStatistic(objective, game, actor, recvr, hidden, new ArrayList<>());
        }

        public long addStatistic(String objective, Game game, String actor, String recvr, boolean hidden,
                                 JsonObject extra) {
            return this.addStatistic(objective, game, actor, recvr, hidden, new ArrayList<>());
        }

        public long addStatistic(String objective, Game game, String actor, String recvr, boolean hidden,
                                 List<String> labels) {
            return this.addStatistic(objective, game, actor, recvr, hidden, new JsonObject(), labels);
        }

        public long addStatistic(String objective, Game game, String actor, String recvr, boolean hidden,
                                 String[] labels) {
            return this.addStatistic(objective, game, actor, recvr, hidden, new JsonObject(), labels);
        }

        public long addStatistic(String objective, Game game, String actor, String recvr, boolean hidden,
                                 JsonObject extra, List<String> labels) {
            return this.addStatistic(objective, game, actor, recvr, hidden, extra, labels.toArray(new String[]{}));
        }

        /**
         * Adds statistic to player.
         *
         * @param objective Type of stat (kill, death, etc.)
         * @param game Game this happened in
         * @param actor Who (nullable-ish) caused this stat to happen.
         * @param recvr Who's profile this stat shows up on
         * @param hidden Is this stat internal, and hidden from public view
         * @param extra Extra data (JSON) to be recorded if necessary such as time to achieve this objective/people involved
         * @param labels Labels or this stat, for example for certain events like tournaments.
         * @return
         */
        public long addStatistic(String objective, Game game, String actor, String recvr, boolean hidden,
                                 JsonObject extra, String... labels) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", objective);
            jsonObject.addProperty("gameType", game.getId());
            jsonObject.addProperty("actor", actor);
            jsonObject.addProperty("receiver", recvr);
            JsonArray jasonLabels = new JsonArray();
            for (String label : labels) {
                jasonLabels.add(new JsonPrimitive(label));
            }
            jsonObject.add("labels", jasonLabels);
            jsonObject.addProperty("hide", hidden);
            jsonObject.add("extra", extra);
            try {
                JsonElement statistic = this.manager.connectionBuilder("statistic").post()
                        .writeJson(jsonObject)
                        .readIn().verifyJsonReturn(true);
                if(statistic == null) {
                    return -1;
                }
                JsonObject parse = statistic.getAsJsonObject();
                // duplicate?
                if(parse.get("warning").getAsBoolean()) {
                    TallyLogger.say("A warning was produced when saving the statistic: ");
                    TallyLogger.say(parse.get("message").getAsString());
                }
                if(parse.get("error").getAsBoolean()) {
                    TallyLogger.say("A error was produced when saving the statistic: ");
                    TallyLogger.say(parse.get("message").getAsString());
                    return -1;
                } else {
                    return parse.get("data").getAsJsonObject().get("id").getAsLong();
                }
            } catch(Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
    }


}
