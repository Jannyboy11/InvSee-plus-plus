package com.janboerman.invsee.spigot.addon.clone;

import java.util.UUID;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.target.Target;

import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClonePlugin extends JavaPlugin {

    private InvseeAPI api;

    @Override
    public void onEnable() {
        Server server = getServer();
        InvseePlusPlus ispp = (InvseePlusPlus) server.getPluginManager().getPlugin("InvSeePlusPlus");
        this.api = ispp.getApi();

        PluginCommand invClone = server.getPluginCommand("invclone");
        invClone.setExecutor(new InvCloneExecutor(this));

        PluginCommand enderClone = server.getPluginCommand("enderclone");
        enderClone.setExecutor(new EnderCloneExecutor(this));
    }

    InvseeAPI getApi() {
        return api;
    }
}
