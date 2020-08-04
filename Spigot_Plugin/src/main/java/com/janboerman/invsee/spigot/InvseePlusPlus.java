package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class InvseePlusPlus extends JavaPlugin implements Listener {

    private InvseeAPI api;

    @Override
    public void onEnable() {
        this.api = InvseeAPI.setup(this);
        Server server = getServer();
        server.getPluginManager().registerEvents(this, this);
        getCommand("invsee").setExecutor(new InvseeCommandExecutor(this));
    }

    public InvseeAPI getApi() {
        return api;
    }

}
