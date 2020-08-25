package com.janboerman.invsee.spigot;

import com.janboerman.invsee.paper.AsyncTabCompleter;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventoryHook;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class InvseePlusPlus extends JavaPlugin {

    private InvseeAPI api;
    private OfflinePlayerProvider offlinePlayerProvider;
    private PerWorldInventoryHook pwiHook;
    private PerWorldInventorySeeApi pwiApi;

    @Override
    public void onEnable() {
        //initialisation
        this.api = InvseeAPI.setup(this);
        this.offlinePlayerProvider = OfflinePlayerProvider.setup(this);

        //PerWorldInventory interop
        PerWorldInventoryHook pwiHook = new PerWorldInventoryHook(this);
        if (pwiHook.trySetup()) {
            if (pwiHook.pwiManagedInventories() || pwiHook.pwiManagedEnderChests()) {
                this.pwiHook = pwiHook;
                this.pwiApi = new PerWorldInventorySeeApi(this, api, pwiHook);
                this.api = pwiApi;
            }
        }

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

        try {
            Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
            pluginManager.registerEvents(new AsyncTabCompleter(this), this);
        } catch (ClassNotFoundException e) {
            getLogger().log(Level.WARNING, "InvSee++ is not running on Paper.");
            getLogger().log(Level.WARNING, "Tab-completion for offline players will not work for all players!");
            getLogger().log(Level.WARNING, "See https://papermc.io/ for more information.");
        }
    }

    public InvseeAPI getApi() {
        return api;
    }

    public OfflinePlayerProvider getOfflinePlayerProvider() {
        return offlinePlayerProvider;
    }

}
