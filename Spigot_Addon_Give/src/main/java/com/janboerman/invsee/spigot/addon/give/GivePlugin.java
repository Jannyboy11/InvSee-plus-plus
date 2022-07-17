package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class GivePlugin extends JavaPlugin {

    private final ItemQueueManager queueManager;

    public GivePlugin() {
        this.queueManager = new ItemQueueManager(this);
    }

    @Override
    public void onEnable() {
        //setup
        saveDefaultConfig();
        ConfigurationSerialization.registerClass(ItemQueue.class, "ItemQueue");
        queueManager.load();

        //listeners
        getServer().getPluginManager().registerEvents(new JoinListener(this, queueManager), this);

        //commands
        InvseePlusPlus invseePlusPlus = (InvseePlusPlus) getServer().getPluginManager().getPlugin("InvSeePlusPlus");
        InvseeAPI api = invseePlusPlus.getApi();

        GiveTabCompleter tabCompleter = new GiveTabCompleter();

        PluginCommand invGiveCommand = getServer().getPluginCommand("invgive");
        invGiveCommand.setExecutor(new InvGiveExecutor(this, api, queueManager));
        invGiveCommand.setTabCompleter(tabCompleter);

        PluginCommand enderGiveCommand = getServer().getPluginCommand("endergive");
        enderGiveCommand.setExecutor(new EnderGiveExecutor(this, api, queueManager));
        enderGiveCommand.setTabCompleter(tabCompleter);
    }

    boolean savePartialInventories() {
        return getConfig().getBoolean("save-partial-inventories", true);
    }

    boolean queueRemainingItems() {
        return getConfig().getBoolean("queue-remaining-items", true);
    }
}
