package com.janboerman.invsee.spigot.addon.clear;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Server server = getServer();

        InvseePlusPlus ispp = (InvseePlusPlus) server.getPluginManager().getPlugin("InvSeePlusPlus");
        InvseeAPI api = ispp.getApi();

        server.getPluginCommand("invclear").setExecutor(new InvClearExecutor(api));
        server.getPluginCommand("enderclear").setExecutor(new EnderClearExecutor(api));
    }

}
