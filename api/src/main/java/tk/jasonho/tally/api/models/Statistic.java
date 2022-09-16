package tk.jasonho.tally.api.models;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.helpers.MapsTo;
import tk.jasonho.tally.api.models.helpers.Model;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class Statistic extends Model {
    public final static AtomicLong handledSoFar = new AtomicLong(0);

    @MapsTo("id")
    public int id;
    @MapsTo("game")
    private Integer game;
    @MapsTo("score")
    private String score;
    @MapsTo("instance")
    private Integer instance;

    public Statistic() {}

    public LabelLink link(TallyStatsManager mgr, Label label, boolean primary) {
        return label.link(mgr, this, primary);
    }

    @SneakyThrows
    public StatLink causalLink(TallyStatsManager mgr, Player player, String role) {
        StatLink build = StatLink.builder()
                .player(player.getId())
                .statistic(this.id)
                .role(role)
                .build();
        build.saveAsCausal(mgr);
        return build;
    }

    public StatLink causalLink(TallyStatsManager mgr, Player player) {
        return this.causalLink(mgr, player, null);
    }

    @SneakyThrows
    public StatLink ownsLink(TallyStatsManager mgr, Player player, String role) {
        StatLink build = StatLink.builder()
                .player(player.getId())
                .statistic(this.id)
                .role(role)
                .build();
        build.saveAsOwns(mgr);
        return build;
    }

    public StatLink ownsLink(TallyStatsManager mgr, Player player) {
        return this.ownsLink(mgr, player, null);
    }

    public StatMetadata attachMetadata(TallyStatsManager mgr, JsonElement data) {
        return this.attachMetadata(mgr, "data", data);
    }

    public StatMetadata attachMetadata(TallyStatsManager mgr, String key, JsonElement data) {
        StatMetadata build = StatMetadata.builder()
                .statistic(this.id)
                .key(key)
                .value(new Gson().toJson(data))
                .build();
        build.save(mgr);
        return build;
    }

    @SneakyThrows
    public void save(TallyStatsManager manager) {
        JsonObject instance = manager.connectionBuilder("statistic")
                .post()
                .writeJson(Model.serialize(this))
                .verifyJsonThrowing()
                .getReadJson()
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject();
        this.updateWith(Model.deserialize(Statistic.class, instance));
        handledSoFar.incrementAndGet();
    }

    public static Statistic of(TallyStatsManager mgr, Game game, String score, Instance instance) {
        Statistic statistic = new Statistic();
        statistic.setGame(game.getId());
        statistic.setScore(score);
        statistic.setInstance(instance.getId());
        statistic.save(mgr);
        return statistic;
    }
}
