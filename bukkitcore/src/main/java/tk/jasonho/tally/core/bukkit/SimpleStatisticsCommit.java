package tk.jasonho.tally.core.bukkit;

import com.google.gson.JsonObject;
import lombok.experimental.Accessors;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.Game;
import tk.jasonho.tally.api.models.Label;
import tk.jasonho.tally.api.models.Player;
import tk.jasonho.tally.api.models.Statistic;
import tk.jasonho.tally.api.util.commits.IStatisticsCommit;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Accessors
public class SimpleStatisticsCommit implements IStatisticsCommit {
    public TallyPlugin tally;
    public Game game;
    public String type;
    public UUID actor;
    public UUID recvr;
    public boolean hidden;
    public JsonObject extras;
    public List<String> labels;
    public Date createdAt;


    public SimpleStatisticsCommit(TallyPlugin tally, Game game, String type,
                                  UUID actor, java.util.UUID recvr, boolean hidden,
                                  JsonObject extras, List<String> labels) {
        this.tally = tally;
        this.game = game;
        this.type = type;
        this.actor = actor;
        this.recvr = recvr;
        this.hidden = hidden;
        this.extras = extras;
        this.labels = labels;
        this.createdAt = new Date();
    }

    public String getLogDescription() {
        return ("type: " + (type == null ? "null" : type))
                + "; actor: " + (actor == null ? "null" : actor.toString())
                + "; recvr: " + (recvr == null ? "null" : recvr.toString());
    }

    @Override
    public void commit() {
        this.tally.optionalLog("Tally tracking: " + this.getLogDescription());
        String actorr = Optional.ofNullable(actor).orElse(DamageTrackModule.ENVIRONMENT).toString();
        String receiverr = Optional.ofNullable(recvr).orElse(DamageTrackModule.ENVIRONMENT).toString();

        TallyStatsManager mgr = TallyPlugin.getInstance().getStatsManager();
        Player causedBy = Player.of(mgr, this.game, actorr);
        Player actedOn = Player.of(mgr, this.game, receiverr);

        Statistic statistic = Statistic.of(mgr, this.game, "1", mgr.getInstance());
        statistic.ownsLink(mgr, actedOn);
        statistic.causalLink(mgr, causedBy);

        Label.of(mgr, type).link(mgr, statistic, true);
        List<Label> createdLabels = labels.stream().map(sl -> Label.of(mgr, sl)).collect(Collectors.toList());
        createdLabels.forEach(l -> l.link(mgr, statistic, false));

        statistic.attachMetadata(mgr, extras);

        JsonObject isHidden = new JsonObject();
        isHidden.addProperty("hidden", hidden);
        statistic.attachMetadata(mgr, "hidden", isHidden);

        // TODO: metadata in extras
        this.tally.optionalLog("Tracked with id: " + statistic.getId() + " (" + this.getLogDescription() + ")");
    }
}
