package tk.jasonho.tally.core.bukkit;

import com.google.gson.JsonObject;
import lombok.Getter;
import tk.jasonho.tally.api.models.*;
import tk.jasonho.tally.api.util.commits.IStatisticsCommit;

import java.util.List;
import java.util.UUID;

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
        this.track(
                new SimpleStatisticsCommit(tally, game, type, actor, recvr, hidden, extras, labels)
        );
    }

    public void track(IStatisticsCommit commit) {
        this.tally.optionalLog("Queueing track task: " + commit.getLogDescription());
        TallyStatisticsTask.submitTask(new TallyStatisticsTask(tally, commit));
        this.tally.optionalLog("Started track task: " + commit.getLogDescription());
    }
}
