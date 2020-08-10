package tk.jasonho.tally.core.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * General purpose simplified task handler
 */
public class TallyThreading {

    private JavaPlugin tally;

    public TallyThreading(JavaPlugin tally) {
        this.tally = tally;
    }

    public List<BukkitTask> getTasks() {
        return Bukkit.getScheduler().getPendingTasks()
                .stream()
                .filter(t ->
                        t.getOwner().getName()
                                .equals(this.tally.getName()))
                .collect(Collectors.toList());
    }

    public BukkitTask async(Runnable run) {
        return Bukkit.getScheduler().runTaskAsynchronously(this.tally, run);
    }

    public BukkitTask asyncLater(Runnable run, long ticksLater) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(this.tally, run, ticksLater);
    }

    public BukkitTask asyncRepeating(Runnable run, long ticksLaterStart, long ticksBetween) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(this.tally, run, ticksLaterStart, ticksBetween);
    }

    public BukkitTask sync(Runnable run) {
        return Bukkit.getScheduler().runTask(this.tally, run);
    }

    public BukkitTask syncLater(Runnable run, long ticksLater) {
        return Bukkit.getScheduler().runTaskLater(this.tally, run, ticksLater);
    }

    public BukkitTask syncRepeating(Runnable run, long ticksLaterStart, long ticksBetween) {
        return Bukkit.getScheduler().runTaskTimer(this.tally, run, ticksLaterStart, ticksBetween);
    }
}
