package tk.jasonho.tally.core.bukkit;

import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.scheduler.BukkitTask;
import tk.jasonho.tally.api.TallyConfiguration;
import tk.jasonho.tally.api.models.Statistic;

import java.util.ArrayList;
import java.util.regex.Pattern;

class TallyCommand extends BukkitCommand {
    private static final String INVALID_USAGE = "Invalid usage! Please double check that you used this command correctly!";

    private TallyPlugin tally;

    {
        this.description = "Manage Tally statistics settings.";
        this.usageMessage = "/tally <list|tasks|stats|instance|summary|addlabel|removelabel|match|caches> [label1 label2 label3...]";
        this.setPermission("tally.manage");
        this.setAliases(new ArrayList<>());
    }

    public TallyCommand(TallyPlugin tally) {
        super("tally");
        this.tally = tally;
    }

    private boolean invalidUsage(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.RED + INVALID_USAGE + " " + this.usageMessage);
        return true;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (strings.length <= 0) {
            return invalidUsage(commandSender);
        }

        String mode = strings[0];
        switch (mode.toLowerCase()) {
            case "list":
                List<String> activeLabels = this.tally.getLabels();
                commandSender.sendMessage(ChatColor.GREEN + "Tally is labelling statistics with these labels:");
                for (String label : activeLabels) {
                    commandSender.sendMessage(ChatColor.GREEN + " - " + label);
                }
                return true;
            case "tasks":
                List<BukkitTask> tasks = this.tally.getTaskManager().getTasks();
                commandSender.sendMessage(ChatColor.YELLOW + "Tally is executing " + tasks.size() + " tasks in the background:");
                for (BukkitTask task : tasks) {
                    commandSender.sendMessage(ChatColor.YELLOW + " " + task.getTaskId() + ". "
                            + "Executing " + ((task.isSync()) ? " synchronously" : "asynchronously") + " "
                            + "for " + task.getOwner().getName());
                }
                return true;
            case "stats":
                commandSender.sendMessage(ChatColor.AQUA + "Tally has attempted to process "
                        + Statistic.handledSoFar.get()
                        + " statistics in this instance.");
                commandSender.sendMessage(ChatColor.AQUA + "Tally has " + TallyStatisticsTask.tasks.size() + " statistics commits.");
                commandSender.sendMessage(ChatColor.AQUA + "Tally has " + TallyStatisticsTask.taskQueue.size() + " statistics commits queued. ");
                commandSender.sendMessage(ChatColor.AQUA + "Tally has " + TallyStatisticsTask.tasks.stream().filter((t) -> !t.started && !t.finished && t.failed == null).count() + " statistics commits ready to be committed.");
                commandSender.sendMessage(ChatColor.AQUA + "Tally has " + TallyStatisticsTask.tasks.stream().filter((t) -> t.started).count() + " statistics commits started.");
                commandSender.sendMessage(ChatColor.AQUA + "Tally has " + TallyStatisticsTask.tasks.stream().filter((t) -> t.finished).count() + " statistics commits completed.");
                commandSender.sendMessage(ChatColor.AQUA + "Tally has " + TallyStatisticsTask.tasks.stream().filter((t) -> t.failed != null).count() + " statistics commits failed.");
                return true;
            case "instance":
                TallyConfiguration configuration = this.tally.getStatsManager().getConfiguration();
                commandSender.sendMessage(ChatColor.GREEN + "Tally Instance Self Identifier: "
                        + this.tally.getStatsManager().getInstance().getSelfid());
                return true;
            case "summary":
                commandSender.sendMessage(ChatColor.GREEN + "Tally Statistics Data for this instance (" +
                        this.tally.getStatsManager().getInstance().getSelfid() + "): " +
                        this.tally.getSummaryURL().replaceAll(
                                Pattern.quote("{INSTANCE}"),
                                this.tally.getStatsManager().getInstance().getSelfid()));
                return true;
            case "addlabel":
                if (strings.length <= 1) {
                    commandSender.sendMessage(ChatColor.YELLOW + "No labels were added!");
                    return true;
                }
                String[] labelsToAdd = Arrays.copyOfRange(strings, 1, strings.length);
                for (String label : labelsToAdd) {
                    this.tally.getLabels().add(label);
                    commandSender.sendMessage(ChatColor.GREEN + "Added label: " + label);
                }
                return true;
            case "removelabel":
                if (strings.length <= 1) {
                    commandSender.sendMessage(ChatColor.YELLOW + "No labels were removed!");
                    return true;
                }
                String[] labelsToRemove = Arrays.copyOfRange(strings, 1, strings.length);
                for (String label : labelsToRemove) {
                    this.tally.getLabels().remove(label);
                    commandSender.sendMessage(ChatColor.RED + "Removed label: " + label);
                }
                return true;
            case "hidden":
                if (strings.length <= 1) {
                    commandSender.sendMessage(ChatColor.YELLOW + "Tagging Hidden Metadata: " + (this.tally.getStatsManager().isTagHiddenMetadata() ? "Enabled" : "Disabled"));
                }
            case "match":
                if (strings.length <= 1) {
                    commandSender.sendMessage(ChatColor.YELLOW + "Tagging Match Metadata: " + (this.tally.getStatsManager().isTagMatches() ? "Enabled" : "Disabled"));
                    commandSender.sendMessage(ChatColor.YELLOW + "Match Tag: " + this.tally.getStatsManager().getMatchTag());
                    commandSender.sendMessage(ChatColor.YELLOW + "Match Metadata: " + this.tally.getStatsManager().matchData.toString());
                }
            case "caches":
                if (strings.length <= 1) {
                    int cleared = this.tally.getStatsManager().clearCaches();
                    commandSender.sendMessage(ChatColor.AQUA + "Caches cleared, size: " + cleared + " objects");
                }
            default:
                return false;
        }
    }
}
