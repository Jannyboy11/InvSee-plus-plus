package com.janboerman.invsee.spigot.addon.clear;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Server server = getServer();

        InvseePlusPlus ispp = (InvseePlusPlus) server.getPluginManager().getPlugin("InvSeePlusPlus");
        InvseeAPI api = ispp.getApi();

        ClearTabCompleter tabCompleter = new ClearTabCompleter();

        PluginCommand invClear = server.getPluginCommand("invclear");
        invClear.setExecutor(new InvClearExecutor(this, api));
        invClear.setTabCompleter(tabCompleter);

        PluginCommand enderClear = server.getPluginCommand("enderclear");
        enderClear.setExecutor(new EnderClearExecutor(this, api));
        enderClear.setTabCompleter(tabCompleter);
    }

    //TODO configuration option to also clear out of the queue created by InvSee++_Give ?

}
