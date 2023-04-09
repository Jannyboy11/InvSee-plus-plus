package com.janboerman.invsee.spigot;

import com.janboerman.invsee.paper.AsyncTabCompleter;
import com.janboerman.invsee.folia.FoliaScheduler;
import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.api.Title;
import com.janboerman.invsee.spigot.api.logging.LogGranularity;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.logging.LogTarget;
import com.janboerman.invsee.spigot.api.target.Target;
/*
import com.janboerman.invsee.spigot.multiverseinventories.MultiverseInventoriesHook;
import com.janboerman.invsee.spigot.multiverseinventories.MultiverseInventoriesSeeApi;
 */
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventoryHook;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InvseePlusPlus extends JavaPlugin {

    private final boolean asyncTabcompleteEvent;

    private InvseeAPI api;
    @Deprecated(forRemoval = true) private OfflinePlayerProvider offlinePlayerProvider;

    private CreationOptions<PlayerInventorySlot> platformCreationOptionsMainInventory;
    private CreationOptions<EnderChestSlot> platformCreationOptionsEnderInventory;

    public InvseePlusPlus() {
        boolean asyncTabCompleteEvent;
        try {
            Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
            asyncTabCompleteEvent = true;
        } catch (ClassNotFoundException e) {
            asyncTabCompleteEvent = false;
        }
        this.asyncTabcompleteEvent = asyncTabCompleteEvent;
    }

    @Override
    public void onEnable() {
        //configuration
        saveDefaultConfig();

        //initialisation
        final Scheduler scheduler = makeScheduler(this);
        final NamesAndUUIDs lookup = new NamesAndUUIDs(this, scheduler);
        final OpenSpectatorsCache cache = new OpenSpectatorsCache();
        Setup setup = Setup.setup(this, scheduler, lookup, cache);
        final InvseePlatform platform = setup.platform();
        final OfflinePlayerProvider playerDatabase = setup.offlinePlayerProvider();

        //TODO @Deprecated
        this.offlinePlayerProvider = playerDatabase;

        //interop
        PerWorldInventoryHook pwiHook;
        //MultiverseInventoriesHook mviHook;
        if (offlinePlayerSupport() && (pwiHook = new PerWorldInventoryHook(this)).trySetup()) {
            if (pwiHook.managesEitherInventory()) {
                this.api = new PerWorldInventorySeeApi(this, lookup, scheduler, cache, platform, pwiHook);
                getLogger().info("Enabled PerWorldInventory integration.");
            }
        }
//        else if (offlinePlayerSupport() && (mviHook = new MultiverseInventoriesHook(this)).trySetup()) {
//            this.api = new MultiverseInventoriesSeeApi(this, api, mviHook);
//            getLogger().info("Enabled Multiverse-Inventories integration.");
//        }
        // else if (MyWorlds)
        // else if (Separe-World-Items)

        else {
            this.api = new InvseeAPI(this, platform, lookup, scheduler, cache);
        }

        assert this.api != null : "did not set the InvseeAPI instance!";

        //set up default creation options
        //TODO if no values are configured, use the platform default CreationOptions, and save these to config.
        //TODO rewrite configuration logic.
        this.platformCreationOptionsMainInventory = platform.defaultInventoryCreationOptions(this);
        this.platformCreationOptionsEnderInventory = platform.defaultEnderChestCreationOptions(this);

        api.setOfflinePlayerSupport(offlinePlayerSupport());
        api.setUnknownPlayerSupport(unknownPlayerSupport());
        api.setMainInventoryTitle(getTitleForInventory());
        api.setEnderInventoryTitle(getTitleForEnderChest());
        api.setMainInventoryMirror(getInventoryMirror());
        api.setEnderInventoryMirror(getEnderChestMirror());
        api.setLogOptions(getLogOptions());

        //commands
        setupCommands();

        //event listeners
        setupEvents(scheduler, playerDatabase);

        //TODO idea: shoulder look functionality. an admin will always see the same inventory that the target player sees.
        //TODO can I make it so that the bottom slots show the target player's inventory slots? would probably need to do some nms hacking

        //bStats
        setupBStats();
    }

    private void setupCommands() {
        InvseeTabCompleter tabCompleter = new InvseeTabCompleter(this);

        PluginCommand invseeCommand = getCommand("invsee");
        PluginCommand enderseeCommand = getCommand("endersee");

        invseeCommand.setExecutor(new InvseeCommandExecutor(this));
        enderseeCommand.setExecutor(new EnderseeCommandExecutor(this));

        invseeCommand.setTabCompleter(tabCompleter);
        enderseeCommand.setTabCompleter(tabCompleter);
    }

    private void setupEvents(Scheduler scheduler, OfflinePlayerProvider playerDatabase) {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new SpectatorInventoryEditListener(), this);

        if (offlinePlayerSupport() && tabCompleteOfflinePlayers()) {
            if (asyncTabcompleteEvent) {
                pluginManager.registerEvents(new AsyncTabCompleter(this, scheduler, playerDatabase), this);
            } else {
                getLogger().log(Level.WARNING, "InvSee++ is not running on a Paper API-enabled server.");
                getLogger().log(Level.WARNING, "Tab-completion for offline players will not work for all players!");
                getLogger().log(Level.WARNING, "See https://papermc.io/ for more information.");
            }
        }
    }

    private void setupBStats() {
        int pluginId = 9309;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SimplePie("Back-end", () -> {
            if (this.api instanceof PerWorldInventorySeeApi) {
                return "PerWorldInventory";
//            } else if (this.api instanceof MultiverseInventoriesSeeApi) {
//                return "Multiverse-Inventories";
            }
            //else if: MyWorlds
            //else if: Separe-World-Items
            else {
                return "Vanilla";
            }
        }));
    }
	
	@Override
	public void onDisable() {
        if (api != null) { //the api can be null if we are running on unsupported server software.
            api.shutDown(); //complete all inventory futures - ensures /invgive and /endergive will still work even if the server shuts down.
        }
	}

    public InvseeAPI getApi() {
        return api;
    }

    @Deprecated(forRemoval = true, since = "0.20.0")
    public OfflinePlayerProvider getOfflinePlayerProvider() {
        return offlinePlayerProvider;
    }

    public boolean tabCompleteOfflinePlayers() {
        return getConfig().getBoolean("tabcomplete-offline-players", asyncTabcompleteEvent);
    }

    public boolean offlinePlayerSupport() {
        return getConfig().getBoolean("enable-offline-player-support", platformCreationOptionsMainInventory.isOfflinePlayerSupported());
    }

    public boolean unknownPlayerSupport() {
        return getConfig().getBoolean("enable-unknown-player-support", platformCreationOptionsMainInventory.isUnknownPlayerSupported());
    }

    public Title getTitleForInventory() {
        String configuredTitle = getConfig().getString("titles.inventory");
        if (configuredTitle == null) {
            return platformCreationOptionsMainInventory.getTitle();
        } else {
            return target -> configuredTitle.replace("<player>", target.toString());
        }
    }

    public Title getTitleForEnderChest() {
        String configuredTitle = getConfig().getString("titles.enderchest");

        if (configuredTitle == null) {
            return platformCreationOptionsEnderInventory.getTitle();
        } else {
            return target -> configuredTitle.replace("<player>", target.toString());
        }
    }

    @Deprecated(forRemoval = true)
    public String getTitleForInventory(Target target) {
        return getConfig().getString("titles.inventory", "<player>'s inventory")
                .replace("<player>", target.toString());
    }

    @Deprecated(forRemoval = true)
    public String getTitleForEnderChest(Target target) {
        return getConfig().getString("titles.enderchest", "<player>'s enderchest")
                .replace("<player>", target.toString());
    }

    public Mirror<PlayerInventorySlot> getInventoryMirror() {
        String template = getConfig().getString("templates.inventory");
        if (template != null) {
            return Mirror.forInventory(template);
        } else {
            return platformCreationOptionsMainInventory.getMirror();
        }
    }

    public Mirror<EnderChestSlot> getEnderChestMirror() {
        String template = getConfig().getString("templates.enderchest");
        if (template != null) {
            return Mirror.forEnderChest(template);
        } else {
            return platformCreationOptionsEnderInventory.getMirror();
        }
    }

    @Deprecated(forRemoval = true)
    public String getInventoryTemplate() {
        return getConfig().getString("templates.inventory",
            "i_00 i_01 i_02 i_03 i_04 i_05 i_06 i_07 i_08\n" +
            "i_09 i_10 i_11 i_12 i_13 i_14 i_15 i_16 i_17\n" +
            "i_18 i_19 i_20 i_21 i_22 i_23 i_24 i_25 i_26\n" +
            "i_27 i_28 i_29 i_30 i_31 i_32 i_33 i_34 i_35\n" +
            "a_b  a_l  a_c  a_h  oh   c    _    _    _   \n" +
            "p_00 p_01 p_02 p_03 p_04 p_05 p_06 p_07 p_08");
    }

    @Deprecated(forRemoval = true)
    public String getEnderChestTemplate() {
        return getConfig().getString("templates.enderchest",
            "e_00 e_01 e_02 e_03 e_04 e_05 e_06 e_07 e_08\n" +
            "e_09 e_10 e_11 e_12 e_13 e_14 e_15 e_16 e_17\n" +
            "e_18 e_19 e_20 e_21 e_22 e_23 e_24 e_25 e_26\n" +
            "e_27 e_28 e_29 e_30 e_31 e_32 e_33 e_34 e_35\n" +
            "e_36 e_37 e_38 e_39 e_40 e_41 e_42 e_43 e_44\n" +
            "e_45 e_46 e_47 e_48 e_49 e_50 e_51 e_52 e_53");
    }

    public LogOptions getLogOptions() {
        FileConfiguration config = getConfig();
        ConfigurationSection logging = config.getConfigurationSection("logging");
        if (logging == null) {
            return platformCreationOptionsMainInventory.getLogOptions();
        } else {
            String granularity = logging.getString("granularity", "LOG_ON_CLOSE");
            LogGranularity logGranularity = LogGranularity.valueOf(granularity);
            List<String> output = logging.getStringList("output");
            EnumSet<LogTarget> logTargets = output.stream()
                    .map(LogTarget::valueOf)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(LogTarget.class)));
            EnumMap<LogTarget, String> formats = new EnumMap<>(LogTarget.class);
            String formatServerLogFile = logging.getString("format-server-log-file");
            if (formatServerLogFile != null) formats.put(LogTarget.SERVER_LOG_FILE, formatServerLogFile);
            String formatPluginLogFile = logging.getString("format-plugin-log-file");
            if (formatPluginLogFile != null) formats.put(LogTarget.PLUGIN_LOG_FILE, formatPluginLogFile);
            String formatSpectatorLogFile = logging.getString("format-spectator-log-file");
            if (formatSpectatorLogFile != null) formats.put(LogTarget.SPECTATOR_LOG_FILE, formatSpectatorLogFile);
            String formatConsole = logging.getString("format-console");
            if (formatConsole != null) formats.put(LogTarget.CONSOLE, formatConsole);
            return LogOptions.of(logGranularity, logTargets, formats);
        }
    }

    private static Scheduler makeScheduler(InvseePlusPlus plugin) {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }

        if (folia) {
            return new FoliaScheduler(plugin);
        } else {
            return new DefaultScheduler(plugin);
        }
    }
}
