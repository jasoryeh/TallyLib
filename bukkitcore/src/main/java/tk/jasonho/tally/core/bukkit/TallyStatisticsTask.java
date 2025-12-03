package tk.jasonho.tally.core.bukkit;

import com.google.gson.JsonObject;
import lombok.experimental.Accessors;
import tk.jasonho.tally.api.models.Game;
import tk.jasonho.tally.api.util.TallyTask;
import tk.jasonho.tally.api.util.commits.StatisticsCommit;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

@Accessors
public class TallyStatisticsTask extends TallyTask {
    public TallyPlugin tally;

    public StatisticsCommit commit;

    // state
    public boolean started = false;
    public boolean finished = false;
    public Throwable failed = null;
    public AtomicLong retries = new AtomicLong(0);

    public static Deque<TallyStatisticsTask> taskQueue = new ConcurrentLinkedDeque<>();
    public static List<TallyStatisticsTask> tasks = new ArrayList<>();

    public TallyStatisticsTask(TallyPlugin tally, Game game, String type,
                               UUID actor, java.util.UUID recvr, boolean hidden,
                               JsonObject extras, List<String> labels, Date date) {
        this(
                tally,
                new SimpleStatisticsCommit(tally, game, type, actor, recvr, hidden, extras, labels, date)
        );
    }

    public TallyStatisticsTask(TallyPlugin tally, StatisticsCommit commit) {
        this.tally = tally;
        this.commit = commit;

        tasks.add(this);
    }

    @Override
    public String getLogDescription() {
        return this.commit.getLogDescription();
    }

    @Override
    public void run() {
        try {
            this.finished = false;
            this.started = true;
            this.commit.commit();
            this.started = false;
            this.finished = true;
        } catch(Exception e) {
            this.failed = e;
            this.finished = false;
            this.started = false;

            this.tally.getLogger().info("Failed to log: " + e.getMessage());
            e.printStackTrace();

            if (retries.getAndIncrement() < this.tally.getMaxRetries()) {
                this.tally.getTaskManager().async(this);
                this.tally.getLogger().info("Set this task to retry: " + this.getLogDescription());
            }
        }
    }

    public static void submitTask(TallyStatisticsTask task) {
        if (task.tally.isUseQueue()) {
            TallyStatisticsTask.taskQueue.push(task);
        } else {
            task.tally.getTaskManager().async(task);
        }
    }

    public static void startHandler(TallyPlugin tally) {
        tally.getTaskManager().asyncRepeating(() -> {
            while (!TallyStatisticsTask.taskQueue.isEmpty()) {
                TallyStatisticsTask task = TallyStatisticsTask.taskQueue.pop();
                try {
                    task.run();
                } catch (Throwable t) {
                    task.tally.getLogger().warning("Failed to log " + task.getLogDescription());

                    throw t;
                }
            }
        }, 20L, 20L * 5);
    }
}
