package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class GivePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        InvseePlusPlus invseePlusPlus = (InvseePlusPlus) getServer().getPluginManager().getPlugin("InvSeePlusPlus");
        InvseeAPI api = invseePlusPlus.getApi();

        GiveTabCompleter tabCompleter = new GiveTabCompleter();

        PluginCommand invGiveCommand = getServer().getPluginCommand("invgive");
        invGiveCommand.setExecutor(new InvGiveExecutor(this, api));
        invGiveCommand.setTabCompleter(tabCompleter);

        PluginCommand enderGiveCommand = getServer().getPluginCommand("endergive");
        enderGiveCommand.setExecutor(new EnderGiveExecutor(this, api));
        enderGiveCommand.setTabCompleter(tabCompleter);
    }

    boolean savePartialInventories() {
        return getConfig().getBoolean("save-partial-inventories", true);
    }
}
