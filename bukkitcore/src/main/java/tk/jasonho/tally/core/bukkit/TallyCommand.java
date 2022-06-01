package tk.jasonho.tally.core.bukkit;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import tk.jasonho.tally.api.models.Statistic;

import java.util.ArrayList;
import java.util.stream.Collectors;

class TallyCommand extends BukkitCommand {
    private static final String INVALID_USAGE = "Invalid usage! Please double check that you used this command correctly!";

    private TallyPlugin tally;

    {
        this.description = "Manage Tally statistics settings.";
        this.usageMessage = "/tally <addlabel|removelabel|list|stats|instance> [label1 label2 label3...]";
        this.setPermission("tally.manage");
        this.setAliases(new ArrayList<>());
    }

    public TallyCommand(TallyPlugin tally) {
        super("tally");
        this.tally = tally;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(strings.length < 2) {
            if(strings.length > 0) {
                String mode = strings[0];
                if(mode.equalsIgnoreCase("list")) {
                    commandSender.sendMessage(ChatColor.GREEN + "Labels: "
                            + this.tally.getLabels().stream().collect(Collectors.joining(", ")));
                    return true;
                } else if(mode.equalsIgnoreCase("tasks")) {
                    commandSender.sendMessage("Tasks: " + this.tally.getTaskManager().getTasks()
                            .stream()
                            .map(t -> t.getTaskId() + "")
                            .collect(Collectors.joining(", ")));
                    return true;
                } else if(mode.equalsIgnoreCase("stats")) {
                    commandSender.sendMessage("Stored " + Statistic.handledSoFar.get() + " stats.");
                } else if(mode.equalsIgnoreCase("instance")) {
                    commandSender.sendMessage("ID: " + this.tally.getStatsManager().getInstance().getSelfid());
                }
            }
            commandSender.sendMessage(ChatColor.RED + INVALID_USAGE + this.usageMessage);
        } else {
            String mode = strings[0];
                for (int i = 0; i < strings.length; i++) {
                    // /tally addlabel label1 label2 label3 label4
                    // index: 0        1      2      3      4
                    if(i > 0) {
                        String label = strings[i];
                        if(mode.equalsIgnoreCase("addlabel")) {
                            this.tally.getLabels().add(label);
                            commandSender.sendMessage(ChatColor.GREEN + "+ " + label);
                        } else if(mode.equalsIgnoreCase("removelabel")) {
                            this.tally.getLabels().remove(label);
                            commandSender.sendMessage(ChatColor.RED + "- " + label);
                        }
                    }
                }
        }
        return true;
    }
}
