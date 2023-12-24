package tk.jasonho.tally.core.bukkit;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import tk.jasonho.tally.api.TallyConfiguration;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.helpers.Model;

import java.util.*;
import java.util.stream.Collectors;


public class TallyPlugin extends JavaPlugin {
    protected static TallyPlugin instance;

    @Getter
    protected TallyStatsManager statsManager;

    @Getter
    protected List<String> labels; // stats labels

    @Getter
    protected TallyThreading taskManager;

    @Getter
    protected TallyOperationHandler operationHandler;

    @Getter
    protected BukkitCombatListener combatListener;

    protected boolean verbose;

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("Tally upping.");

        this.saveDefaultConfig();
        this.reloadConfig();
        this.getConfig();

        this.verbose = this.getConfig().getBoolean("verbose", false);
        Model.verbose = this.verbose;

        this.labels = (this.getConfig().contains("labels") && this.getConfig().isList("labels")) ?
                this.getConfig().getStringList("labels") : new ArrayList<>();

        this.statsManager = new TallyStatsManager(
                new TallyConfiguration(
                        this.getConfig().getString("host", null),
                        this.getConfig().getString("auth"),
                        ((List<String>) this.getConfig().getList("labels", new ArrayList<>())))
        );

        this.taskManager = new TallyThreading(this);
        this.operationHandler = new TallyOperationHandler(this);

        // track default damage stuff
        this.loadTracker(new DefaultDamageTrackModule(this.operationHandler, "null"));

        // track default pvp/login, the usual events
        this.combatListener = new BukkitCombatListener(this.operationHandler);
        this.registerTallyListener(this.combatListener);

        this.getServer().getCommandMap().register("tally", new TallyCommand(this));

        // verification
        this.statsManager.test();

        this.getLogger().info("Tally upped.");
    }

    @Override
    public void onDisable() {
        this.unloadTracker();
        this.unregisterTallyListener(this.combatListener);
        this.getLogger().info("Tally downed.");
    }

    public static TallyPlugin getInstance() {
        return instance;
    }

    public void registerTallyListener(TallyListener listener) {
        this.registerTallyListener(listener, this);
    }

    public void registerTallyListener(TallyListener listener, Plugin onBehalfOf) {
        Bukkit.getPluginManager().registerEvents(listener, onBehalfOf);
    }

    public void unregisterTallyListener(TallyListener listener) {
        HandlerList.unregisterAll(listener);
    }

    /**
     * This needs to be overwritten if functions or events
     * in the DefaultDamageTrackModule class need to be modified or added to.
     * (because some plugins might have special functions that need additional help to
     * detect damage between players such as custom shooter games, or magic abilities
     * that do not count in the traditional player damage events).
     *
     * Notice how `damageTrackModule` is only defined once in the onEnable.
     *
     * This is intentional since it only listens for vanilla behavior.
     */
    @Getter
    @Setter
    protected DamageTrackModule damageTrackModule;

    public void loadTracker(DamageTrackModule module) {
        this.optionalLog("Registering match damage tracker.");
        this.registerTallyListener(module);
        this.setDamageTrackModule(module);
    }

    public void unloadTracker() {
        Bukkit.getLogger().info("Unregistering damage tracker.");
        HandlerList.unregisterAll(this.getDamageTrackModule());
    }

    public void log(Object... object) {
        this.log(Arrays.stream(object).map(Object::toString).collect(Collectors.joining(" ")));
    }

    public void log(String string) {
        this.getLogger().info("[Tally] " + string);
    }

    public void optionalLog(Object... object) {
        this.optionalLog(Arrays.stream(object).map(Object::toString).collect(Collectors.joining(" ")));
    }

    public void optionalLog(String string) {
        if(this.verbose) {
            this.getLogger().info("[Verbose] " + string);
        } else {
            this.getLogger().finest("[Verbose (off)] " + string);
        }
    }

}
