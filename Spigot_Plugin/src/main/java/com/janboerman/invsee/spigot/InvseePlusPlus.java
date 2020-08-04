package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class InvseePlusPlus extends JavaPlugin {

    private InvseeAPI api;

    @Override
    public void onEnable() {
        this.api = InvseeAPI.setup(this);
        getCommand("invsee").setExecutor(new InvseeCommandExecutor(this));
    }

    public InvseeAPI getApi() {
        return api;
    }

}
