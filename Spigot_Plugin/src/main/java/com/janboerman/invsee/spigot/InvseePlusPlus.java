package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class InvseePlusPlus extends JavaPlugin {

    private InvseeAPI api;
    private OfflinePlayerProvider offlinePlayerProvider;

    @Override
    public void onEnable() {
        this.api = InvseeAPI.setup(this);
        this.offlinePlayerProvider = OfflinePlayerProvider.setup(this);

        PluginCommand invseeCommand = getCommand("invsee");
        PluginCommand enderseeCommand = getCommand("endersee");

        invseeCommand.setExecutor(new InvseeCommandExecutor(this));
        enderseeCommand.setExecutor(new EnderseeCommandExecutor(this));

        InvseeTabCompleter tabCompleter = new InvseeTabCompleter(this);
        invseeCommand.setTabCompleter(tabCompleter);
        enderseeCommand.setTabCompleter(tabCompleter);

        try {
            Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
            getServer().getPluginManager().registerEvents(new AsyncTabCompleter(this), this);
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
