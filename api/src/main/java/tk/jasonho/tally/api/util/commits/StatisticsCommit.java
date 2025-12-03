package tk.jasonho.tally.api.util.commits;

import java.util.Date;

public abstract class StatisticsCommit implements IStatisticsCommit {
    public Date at;

    public StatisticsCommit(Date at) {
        this.at = at;
    }
}
