package com.janboerman.invsee.spigot;

import com.janboerman.invsee.paper.AsyncTabCompleter;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.multiverseinventories.MultiverseInventoriesHook;
import com.janboerman.invsee.spigot.multiverseinventories.MultiverseInventoriesSeeApi;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventoryHook;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class InvseePlusPlus extends JavaPlugin {

    private InvseeAPI api;
    private OfflinePlayerProvider offlinePlayerProvider;

    @Override
    public void onEnable() {
        //configuration
        saveDefaultConfig();

        //initialisation
        Setup setup = Setup.setup(this);
        this.api = setup.api();
        this.offlinePlayerProvider = setup.offlinePlayerProvider();

        //interop
        PerWorldInventoryHook pwiHook;
        MultiverseInventoriesHook mviHook;
        if (offlinePlayerSupport() && (pwiHook = new PerWorldInventoryHook(this)).trySetup()) {
            if (pwiHook.managesEitherInventory()) {
                this.api = new PerWorldInventorySeeApi(this, api, pwiHook);
                getLogger().info("Enabled PerWorldInventory integration.");
            }
        }
//        else if (offlinePlayerSupport() && (mviHook = new MultiverseInventoriesHook(this)).trySetup()) {
//            this.api = new MultiverseInventoriesSeeApi(this, api, mviHook);
//            getLogger().info("Enabled Multiverse-Inventories integration.");
//        }
        // else if (MyWorlds)
        // else if (Separe-World-Items)

        //commands
        PluginCommand invseeCommand = getCommand("invsee");
        PluginCommand enderseeCommand = getCommand("endersee");

        invseeCommand.setExecutor(new InvseeCommandExecutor(this));
        enderseeCommand.setExecutor(new EnderseeCommandExecutor(this));

        InvseeTabCompleter tabCompleter = new InvseeTabCompleter(this);
        invseeCommand.setTabCompleter(tabCompleter);
        enderseeCommand.setTabCompleter(tabCompleter);

        //event listeners
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new SpectatorInventoryEditListener(), this);

        if (offlinePlayerSupport() && tabCompleteOfflinePlayers()) {
            try {
                Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
                pluginManager.registerEvents(new AsyncTabCompleter(this), this);
            } catch (ClassNotFoundException e) {
                getLogger().log(Level.WARNING, "InvSee++ is not running on a Paper API-enabled server.");
                getLogger().log(Level.WARNING, "Tab-completion for offline players will not work for all players!");
                getLogger().log(Level.WARNING, "See https://papermc.io/ for more information.");
            }
        }

        //TODO idea: shoulder look functionality. an admin will always see the same inventory that the target player sees.
        //TODO can I make it so that the bottom slots show the target player's inventory slots? would probably need to do some nms hacking

        //bStats
        int pluginId = 9309;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SimplePie("Back-end", () -> {
            if (this.api instanceof PerWorldInventorySeeApi) {
                return "PerWorldInventory";
            } else if (this.api instanceof MultiverseInventoriesSeeApi) {
                return "Multiverse-Inventories";
            }
            //else if: MyWorlds
            //else if: Separe-World-Items
            else {
                return "Vanilla";
            }
        }));
    }

    public InvseeAPI getApi() {
        return api;
    }

    public OfflinePlayerProvider getOfflinePlayerProvider() {
        return offlinePlayerProvider;
    }

    public boolean tabCompleteOfflinePlayers() {
        return getConfig().getBoolean("tabcomplete-offline-players", true);
    }

    public boolean offlinePlayerSupport() {
        return getConfig().getBoolean("enable-offline-player-support", true);
    }
}
