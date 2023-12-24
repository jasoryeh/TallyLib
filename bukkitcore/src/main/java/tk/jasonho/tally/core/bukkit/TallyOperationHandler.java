package tk.jasonho.tally.core.bukkit;

import com.google.gson.JsonObject;
import lombok.Getter;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.*;
import tk.jasonho.tally.api.util.TallyUtils;
import tk.jasonho.tally.api.util.Threading;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TallyOperationHandler {

    @Getter
    private TallyPlugin tally;
    private Game game;
    public TallyOperationHandler(TallyPlugin tally) {
        this.tally = tally;
        this.game = Game.ofTag(tally.getStatsManager(), tally.getConfig().getString("gameTag", "mc-java"));
    }

    /**
     * Basic statistic tracking
     *
     * Anything that can be specifiable in the
     * track(type, actor, recvr, hidden, extras, labels)
     * specifiable
     *
     * @param type statistic type/objective
     * @param actor Who caused this to happen
     * @param recvr Whose profile this stat should belong to and show up on
     */
    public void track(String type, UUID actor, UUID recvr) {
        this.track(type, actor, recvr, false);
    }

    public void track(String type, UUID actor, UUID recvr, boolean hidden) {
        this.track(type, actor, recvr, hidden, new JsonObject());
    }

    public void track(String type, UUID actor, UUID recvr, JsonObject extras) {
        this.track(type, actor, recvr, false, extras);
    }

    public void track(String type, UUID actor, UUID recvr, boolean hidden, JsonObject extras) {
        this.track(type, actor, recvr, hidden, extras, this.tally.getLabels());
    }

    /**
     * Detailed statistic tracking
     *
     * @param type Statistic type/objective
     * @param actor Who caused this to happen
     * @param recvr Whose profile this stat should belong to and show up on
     * @param hidden Whether this is a publicly accessible statistic
     * @param extras Extra important information
     * @param labels Labels this statistic
     */
    public void track(String type, UUID actor, UUID recvr, boolean hidden, JsonObject extras, List<String> labels) {
        extras.addProperty("instance_host", TallyUtils.getSelfIP());
        String information = ("type: " + (type == null ? "null" : type))
                + "; actor: " + (actor == null ? "null" : actor.toString())
                + "; recvr: " + (recvr == null ? "null" : recvr.toString());

        this.tally.optionalLog("Starting track task: " + information);
        this.tally.getTaskManager().async(() -> {
            this.tally.optionalLog("Tally tracking: " + information);
            String actorr = Optional.of(actor).orElse(DamageTrackModule.ENVIRONMENT).toString();
            String receiverr = Optional.of(recvr).orElse(DamageTrackModule.ENVIRONMENT).toString();

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
            this.tally.optionalLog("Tracked with id: " + statistic.getId() + " (" + information + ")");
        });
        this.tally.optionalLog("Started track task: " + information);
    }

}
