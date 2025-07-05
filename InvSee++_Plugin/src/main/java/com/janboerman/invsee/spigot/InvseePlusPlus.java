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
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette;
import com.janboerman.invsee.spigot.api.target.Target;
/*
import com.janboerman.invsee.spigot.multiverseinventories.MultiverseInventoriesHook;
import com.janboerman.invsee.spigot.multiverseinventories.MultiverseInventoriesSeeApi;
 */
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.ConstantTitle;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventoryHook;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvseePlusPlus extends JavaPlugin implements com.janboerman.invsee.spigot.api.InvseePlusPlus {

    public static final String TABCOMPLETION_PERMISSION = "invseeplusplus.tabcomplete";

    private final boolean asyncTabcompleteEvent;

    private InvseeAPI api;
    private InvseePlatform platform;
    @Deprecated/*(forRemoval = true)*/ private OfflinePlayerProvider offlinePlayerProvider; //TODO remove in 1.0.0

    private CreationOptions<PlayerInventorySlot> platformCreationOptionsMainInventory;
    private CreationOptions<EnderChestSlot> platformCreationOptionsEnderInventory;
    private boolean dirtyConfig = false;

    private Metrics bstats;

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
        //if config is absent, save default config
        saveDefaultConfig();

        //initialisation
        final Scheduler scheduler = makeScheduler(this);
        final NamesAndUUIDs lookup = new NamesAndUUIDs(this, scheduler);
        final OpenSpectatorsCache cache = new OpenSpectatorsCache();
        Setup setup = Setup.setup(this, scheduler, lookup, cache);
        platform = setup.platform();
        final OfflinePlayerProvider playerDatabase = setup.offlinePlayerProvider();

        //set up default creation options
        this.platformCreationOptionsMainInventory = platform.defaultInventoryCreationOptions(this);
        this.platformCreationOptionsEnderInventory = platform.defaultEnderChestCreationOptions(this);

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

        //set up api creation options
        FileConfiguration config = loadConfig();
        tabCompleteOfflinePlayers(config); //set config value
        api.setOfflinePlayerSupport(offlinePlayerSupport(config));
        api.setUnknownPlayerSupport(unknownPlayerSupport(config));
        api.setMainInventoryTitle(getTitleForInventory(config));
        api.setEnderInventoryTitle(getTitleForEnderChest(config));
        api.setMainInventoryMirror(getInventoryMirror(config));
        api.setEnderInventoryMirror(getEnderChestMirror(config));
        api.setLogOptions(getLogOptions(config));
        api.setPlaceholderPalette(getPlaceholderPalette(platform, config));

        //commands
        setupCommands();

        //event listeners
        setupEvents(scheduler, playerDatabase);

        //bStats
        setupBStats();

        //idea: shoulder look functionality. an admin will always see the same inventory that the target player sees.
        //can I make it so that the bottom slots show the target player's inventory slots? would probably need to do some nms hacking

        //save new config options
        if (dirtyConfig) {
            try {
                config.save(getConfigFile());
                dirtyConfig = false;
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not update config file!", e);
            }
        }
    }

    private void setupCommands() {
        InvseeTabCompleter tabCompleter = new InvseeTabCompleter(this);

        PluginCommand invseeCommand = getCommand("invsee");
        PluginCommand enderseeCommand = getCommand("endersee");
        PluginCommand reloadCommand = getCommand("invseeplusplusreload");

        invseeCommand.setExecutor(new InvseeCommandExecutor(this));
        enderseeCommand.setExecutor(new EnderseeCommandExecutor(this));
        reloadCommand.setExecutor(new ReloadCommandExecutor(this));

        invseeCommand.setTabCompleter(tabCompleter);
        enderseeCommand.setTabCompleter(tabCompleter);
    }

    private void setupEvents(Scheduler scheduler, OfflinePlayerProvider playerDatabase) {
        // Pass FileConfiguration config parameter?

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new SpectatorInventoryEditListener(), this);

        if (offlinePlayerSupport() && tabCompleteOfflinePlayers()) {
            if (asyncTabcompleteEvent) {
                pluginManager.registerEvents(new AsyncTabCompleter(this, scheduler, playerDatabase), this);
            }
        }
    }

    private void setupBStats() {
        int pluginId = 9309;
        bstats = new Metrics(this, pluginId);
        bstats.addCustomChart(new SimplePie("Back-end", () -> {
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

        if (bstats != null) {
            bstats.shutdown();
        }
	}

    /**
     * Get the InvseeAPI instance.
     * @return the api
     */
    public InvseeAPI getApi() {
        return api;
    }

    /**
     * Internal api.
     * @return a stream containing all materials the server knows about
     */
    //Should probably move this method somewhere else.
    //I do like this location though because it's accessible for built-in addons, but not public api.
    public Stream<Material> itemMaterials() {
        return platform.materials();
    }

    /**
     * Get whether InvSee++ should tabcomplete names of players who are offline.
     * @return true if tabcompletion is enabled for offline players, otherwise false
     */
    public boolean tabCompleteOfflinePlayers() {
        return tabCompleteOfflinePlayers(getConfig());
    }

    public boolean tabCompleteOfflinePlayers(FileConfiguration config) {
        Object value = config.get("tabcomplete-offline-players");
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        } else {
            dirtyConfig = true;
            config.set("tabcomplete-offline-players", asyncTabcompleteEvent);
            return asyncTabcompleteEvent;
        }
    }

    /**
     * Get whether InvSee++ should support spectating inventories of players who are offline.
     * @return true if spectating offline players' inventories is supported, otherwise false
     */
    public boolean offlinePlayerSupport() {
        return offlinePlayerSupport(getConfig());
    }

    public boolean offlinePlayerSupport(FileConfiguration config) {
        Object value = config.get("enable-offline-player-support");
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        } else {
            dirtyConfig = true;
            boolean offlinePlayerSupport = platformCreationOptionsMainInventory.isOfflinePlayerSupported();
            config.set("enable-offline-player-support", offlinePlayerSupport);
            return offlinePlayerSupport;
        }
    }

    /**
     * Get whether InvSee++ should support spectating inventories of players who have not played on the server before.
     * @return true if spectating unknown players is supported, otherwise false
     */
    public boolean unknownPlayerSupport() {
        return unknownPlayerSupport(getConfig());
    }

    public boolean unknownPlayerSupport(FileConfiguration config) {
        Object value = config.get("enable-unknown-player-support");
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        } else {
            dirtyConfig = true;
            boolean unknownPlayerSupport = platformCreationOptionsMainInventory.isUnknownPlayerSupported();
            config.set("enable-unknown-player-support", unknownPlayerSupport);
            return unknownPlayerSupport;
        }
    }

    /**
     * Get the Title used for {@link com.janboerman.invsee.spigot.api.MainSpectatorInventory}s.
     * @return the title
     */
    public Title getTitleForInventory() {
        return getTitleForInventory(getConfig());
    }

    public Title getTitleForInventory(FileConfiguration config) {
        String configuredTitle = config.getString("titles.inventory");
        if (configuredTitle == null) {
            dirtyConfig = true;
            Title value = platformCreationOptionsMainInventory.getTitle();
            if (value == Title.defaultMainInventory())
                config.set("titles.inventory", "<player>'s inventory");
            else if (value instanceof ConstantTitle)
                config.set("titles.inventory", ((ConstantTitle) value).getTitle());
            return value;
        } else {
            return target -> configuredTitle.replace("<player>", target.toString());
        }
    }

    /**
     * Get the title used for {@link com.janboerman.invsee.spigot.api.EnderSpectatorInventory}s.
     * @return the title
     */
    public Title getTitleForEnderChest() {
        return getTitleForEnderChest(getConfig());
    }

    public Title getTitleForEnderChest(FileConfiguration config) {
        String configuredTitle = config.getString("titles.enderchest");

        if (configuredTitle == null) {
            dirtyConfig = true;
            Title value = platformCreationOptionsEnderInventory.getTitle();
            if (value == Title.defaultEnderInventory())
                config.set("titles.enderchest", "<player>'s enderchest");
            else if (value instanceof ConstantTitle)
                config.set("titles.enderchest", ((ConstantTitle) value).getTitle());
            return value;
        } else {
            return target -> configuredTitle.replace("<player>", target.toString());
        }
    }

    /**
     * Get the Mirror used for {@link com.janboerman.invsee.spigot.api.MainSpectatorInventory}s.
     * @return the mirror
     */
    public Mirror<PlayerInventorySlot> getInventoryMirror() {
        return getInventoryMirror(getConfig());
    }

    public Mirror<PlayerInventorySlot> getInventoryMirror(FileConfiguration config) {
        String template = config.getString("templates.inventory");
        if (template != null) {
            return Mirror.forInventory(template);
        } else {
            dirtyConfig = true;
            Mirror<PlayerInventorySlot> value = platformCreationOptionsMainInventory.getMirror();
            config.set("templates.inventory", Mirror.toInventoryTemplate(value));
            return value;
        }
    }

    /**
     * Get the Mirror used for {@link com.janboerman.invsee.spigot.api.EnderSpectatorInventory}s.
     * @return the mirror
     */
    public Mirror<EnderChestSlot> getEnderChestMirror() {
        return getEnderChestMirror(getConfig());
    }

    public Mirror<EnderChestSlot> getEnderChestMirror(FileConfiguration config) {
        String template = config.getString("templates.enderchest");
        if (template != null) {
            return Mirror.forEnderChest(template);
        } else {
            dirtyConfig = true;
            Mirror<EnderChestSlot> value = platformCreationOptionsEnderInventory.getMirror();
            config.set("templates.enderchest", Mirror.toEnderChestTemplate(value));
            return value;
        }
    }

    /**
     * Get the logging options.
     * @return the logging otpions
     */
    public LogOptions getLogOptions() {
        return getLogOptions(getConfig());
    }

    public LogOptions getLogOptions(FileConfiguration config) {
        ConfigurationSection loggingSection = config.getConfigurationSection("logging");
        if (loggingSection == null) {
            dirtyConfig = true;
            LogOptions value = platformCreationOptionsMainInventory.getLogOptions();
            loggingSection = config.createSection("logging");
            loggingSection.set("granularity", value.getGranularity().name());
            loggingSection.set("output", value.getTargets().stream().map(LogTarget::name).collect(Collectors.toList()));
            loggingSection.set("format-server-log-file", value.getFormat(LogTarget.SERVER_LOG_FILE));
            loggingSection.set("format-plugin-log-file", value.getFormat(LogTarget.PLUGIN_LOG_FILE));
            loggingSection.set("format-spectator-log-file", value.getFormat(LogTarget.SERVER_LOG_FILE));
            loggingSection.set("format-console", value.getFormat(LogTarget.CONSOLE));
            return value;
        } else {
            String granularity = loggingSection.getString("granularity", "LOG_ON_CLOSE");
            LogGranularity logGranularity = LogGranularity.valueOf(granularity);
            List<String> output = loggingSection.getStringList("output");
            EnumSet<LogTarget> logTargets = output.stream()
                    .map(LogTarget::valueOf)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(LogTarget.class)));
            EnumMap<LogTarget, String> formats = new EnumMap<>(LogTarget.class);
            String formatServerLogFile = loggingSection.getString("format-server-log-file");
            if (formatServerLogFile != null) formats.put(LogTarget.SERVER_LOG_FILE, formatServerLogFile);
            String formatPluginLogFile = loggingSection.getString("format-plugin-log-file");
            if (formatPluginLogFile != null) formats.put(LogTarget.PLUGIN_LOG_FILE, formatPluginLogFile);
            String formatSpectatorLogFile = loggingSection.getString("format-spectator-log-file");
            if (formatSpectatorLogFile != null) formats.put(LogTarget.SPECTATOR_LOG_FILE, formatSpectatorLogFile);
            String formatConsole = loggingSection.getString("format-console");
            if (formatConsole != null) formats.put(LogTarget.CONSOLE, formatConsole);
            return LogOptions.of(logGranularity, logTargets, formats);
        }
    }

    public PlaceholderPalette getPlaceholderPalette() {
        return getPlaceholderPalette(platform, getConfig());
    }

    public PlaceholderPalette getPlaceholderPalette(InvseePlatform platform, FileConfiguration config) {
        String paletteName = config.getString("placeholder-palette");
        PlaceholderPalette palette;
        if (paletteName == null) {
            dirtyConfig = true;
            palette = platformCreationOptionsMainInventory.getPlaceholderPalette();
            config.set("placeholder-palette", palette.toString());
        } else {
            palette = platform.getPlaceholderPalette(paletteName);
        }
        return palette;
    }

    private File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }

    private FileConfiguration loadConfig() {
        return YamlConfiguration.loadConfiguration(getConfigFile());
    }

    private static Scheduler makeScheduler(InvseePlusPlus plugin) {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
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

    /** @deprecated use {@link #getTitleForInventory()} instead. */
    @Deprecated//(forRemoval = true, since = "0.21.0") //TODO remove in 1.0
    public String getTitleForInventory(Target target) {
        return getConfig().getString("titles.inventory", "<player>'s inventory")
                .replace("<player>", target.toString());
    }

    /** @deprecated use {@link #getTitleForEnderChest()} instead.*/
    @Deprecated//(forRemoval = true, since = "0.21.0") //TODO remove in 1.0
    public String getTitleForEnderChest(Target target) {
        return getConfig().getString("titles.enderchest", "<player>'s enderchest")
                .replace("<player>", target.toString());
    }

    /** @deprecated use {@link #getInventoryMirror()} instead. */
    @Deprecated//(forRemoval = true, since = "0.21.0") //TODO remove in 1.0
    public String getInventoryTemplate() {
        return getConfig().getString("templates.inventory",
                "i_00 i_01 i_02 i_03 i_04 i_05 i_06 i_07 i_08\n" +
                "i_09 i_10 i_11 i_12 i_13 i_14 i_15 i_16 i_17\n" +
                "i_18 i_19 i_20 i_21 i_22 i_23 i_24 i_25 i_26\n" +
                "i_27 i_28 i_29 i_30 i_31 i_32 i_33 i_34 i_35\n" +
                "a_b  a_l  a_c  a_h  oh   c    _    _    _   \n" +
                "p_00 p_01 p_02 p_03 p_04 p_05 p_06 p_07 p_08");
    }

    /** @deprecated use {@link #getEnderChestMirror()} instead. */
    @Deprecated//(forRemoval = true, since = "0.21.0") //TODO remove in 1.0
    public String getEnderChestTemplate() {
        return getConfig().getString("templates.enderchest",
                "e_00 e_01 e_02 e_03 e_04 e_05 e_06 e_07 e_08\n" +
                "e_09 e_10 e_11 e_12 e_13 e_14 e_15 e_16 e_17\n" +
                "e_18 e_19 e_20 e_21 e_22 e_23 e_24 e_25 e_26\n" +
                "e_27 e_28 e_29 e_30 e_31 e_32 e_33 e_34 e_35\n" +
                "e_36 e_37 e_38 e_39 e_40 e_41 e_42 e_43 e_44\n" +
                "e_45 e_46 e_47 e_48 e_49 e_50 e_51 e_52 e_53");
    }

    /** @deprecated use your own player database instead. */
    @Deprecated//(forRemoval = true, since = "0.22.0") //TODO remove in 1.0
    public OfflinePlayerProvider getOfflinePlayerProvider() {
        return offlinePlayerProvider;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "Oh no! It looks like InvSee++ didn't start correctly!");
        sender.sendMessage(ChatColor.YELLOW + "Most likely this is a Minecraft/InvSee++ version mismatch.");
        sender.sendMessage(ChatColor.YELLOW + "Check your logs for more information.");
        return true;
    }
}
