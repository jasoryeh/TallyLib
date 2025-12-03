package tk.jasonho.tally.api.util.commits;

import tk.jasonho.tally.api.util.IHasLogDescription;

public interface IStatisticsCommit extends IHasLogDescription {
    public void commit();
}
