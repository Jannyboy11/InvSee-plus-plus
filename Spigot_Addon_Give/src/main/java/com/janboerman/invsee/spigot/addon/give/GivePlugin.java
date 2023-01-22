package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.addon.give.common.GiveApi;
import com.janboerman.invsee.spigot.api.Exempt;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class GivePlugin extends JavaPlugin {

    private final ItemQueueManager queueManager;

    private InvseePlusPlus invseePlusPlus;
    private InvseeAPI invseeApi;

    public GivePlugin() {
        this.queueManager = new ItemQueueManager(this);
    }

    @Override
    public void onEnable() {
        //setup
        saveDefaultConfig();
        ConfigurationSerialization.registerClass(ItemQueue.class, "ItemQueue");
        queueManager.load();
        Setup setup = Setup.setup(this);
        GiveApi giveApi = setup.getGiveApi();

        invseePlusPlus = (InvseePlusPlus) getServer().getPluginManager().getPlugin("InvSeePlusPlus");
        invseeApi = invseePlusPlus.getApi();

        //listeners
        getServer().getPluginManager().registerEvents(new JoinListener(this, queueManager), this);

        //commands
        GiveTabCompleter tabCompleter = new GiveTabCompleter();

        PluginCommand invGiveCommand = getServer().getPluginCommand("invgive");
        invGiveCommand.setExecutor(new InvGiveExecutor(this, invseeApi, giveApi, queueManager));
        invGiveCommand.setTabCompleter(tabCompleter);

        PluginCommand enderGiveCommand = getServer().getPluginCommand("endergive");
        enderGiveCommand.setExecutor(new EnderGiveExecutor(this, invseeApi, giveApi, queueManager));
        enderGiveCommand.setTabCompleter(tabCompleter);
    }

    boolean savePartialInventories() {
        return getConfig().getBoolean("save-partial-inventories", true);
    }

    boolean queueRemainingItems() {
        return getConfig().getBoolean("queue-remaining-items", true);
    }

    boolean offlinePlayerSupport() {
        FileConfiguration config = getConfig();

        Object value = config.get("offline-player-support");
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String s = (String) value;
            if ("true".equalsIgnoreCase(s)) {
                return true;
            } else if ("false".equalsIgnoreCase(s)) {
                return false;
            } else if ("same_as_Invsee++".equalsIgnoreCase(s)) {
                return invseePlusPlus.offlinePlayerSupport();
            } else if ("same_as_InvseeAPI".equalsIgnoreCase(s)) {
                return invseeApi.offlinePlayerSupport();
            }
        }

        //fallback
        return true;
    }

    boolean unknownPlayerSupport() {
        FileConfiguration config = getConfig();

        Object value = config.get("unknown-player-support");
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String s = (String) value;
            if ("true".equalsIgnoreCase(s)) {
                return true;
            } else if ("false".equalsIgnoreCase(s)) {
                return false;
            } else if ("same_as_Invsee++".equalsIgnoreCase(s)) {
                return invseePlusPlus.unknownPlayerSupport();
            } else if ("same_as_InvseeAPI".equalsIgnoreCase(s)) {
                return invseeApi.unknownPlayerSupport();
            }
        }

        //fallback
        return true;
    }

    boolean bypassExemptInvsee(CommandSender sender) {
        Boolean bypass = bypassExempt();
        if (bypass == null) {
            return sender.hasPermission(Exempt.BYPASS_EXEMPT_INVENTORY);
        } else {
            return bypass;
        }
    }

    boolean bypassExemptEndersee(CommandSender sender) {
        Boolean bypass = bypassExempt();
        if (bypass == null) {
            return sender.hasPermission(Exempt.BYPASS_EXEMPT_ENDERCHEST);
        } else {
            return bypass;
        }
    }

    private Boolean bypassExempt() {
        FileConfiguration config = getConfig();

        Object value = config.get("bypass-exempt");
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String s = (String) value;
            if ("true".equalsIgnoreCase(s)) {
                return true;
            } else if ("false".equalsIgnoreCase(s)) {
                return false;
            }
        }

        return null;
    }

}
