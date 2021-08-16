package tk.jasonho.tally.core.bukkit;

import com.google.gson.JsonObject;
import lombok.Getter;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.util.Threading;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TallyOperationHandler {

    @Getter
    private TallyPlugin tally;
    public TallyOperationHandler(TallyPlugin tally) {
        this.tally = tally;
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
        String information = ("type: " + type == null ? "null" : type)
                + "; actor: " + (actor == null ? "null" : actor.toString())
                + "; recvr: " + (recvr == null ? "null" : recvr.toString());

        TallyStatsManager statsManager = TallyPlugin.getInstance().getStatsManager();
        TallyStatsManager.DataAccessor access = statsManager.access();

        String actorr = actor == null || actor.equals(DamageTrackModule.ENVIRONMENT) ? null : actor.toString();
        String receiverr = recvr == null || recvr.equals(DamageTrackModule.ENVIRONMENT) ? null : recvr.toString();

        this.tally.optionalLog("Starting track task: " + information);
        this.tally.getTaskManager().async(() -> {
            this.tally.optionalLog("Tally tracking: " + information);
            long id;
            while((id =
                    access.addStatistic(
                            type,
                            access.getGame(TallyPlugin.TALLY_MINECRAFT_JAVA_EDITION),
                            actorr,
                            receiverr,
                            hidden,
                            extras,
                            labels))
                    == -1) {
                this.tally.getLogger().warning("Failed tracking: " + information + "(-1); will retry.");
                Threading.sleep(5000);
            }
            this.tally.optionalLog("Tracked with id: " + id + " (" + information + ")");
            this.tally.optionalLog("Tally tracked: " + information);
        });
        this.tally.optionalLog("Started track task: " + information);
    }

}
