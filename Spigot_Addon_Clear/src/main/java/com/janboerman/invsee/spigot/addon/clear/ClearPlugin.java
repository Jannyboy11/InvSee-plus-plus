package com.janboerman.invsee.spigot.addon.clear;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.Exempt;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearPlugin extends JavaPlugin {

    private InvseePlusPlus ispp;
    private InvseeAPI api;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Server server = getServer();

        ispp = (InvseePlusPlus) server.getPluginManager().getPlugin("InvSeePlusPlus");
        api = ispp.getApi();

        ClearTabCompleter tabCompleter = new ClearTabCompleter();

        PluginCommand invClear = server.getPluginCommand("invclear");
        invClear.setExecutor(new InvClearExecutor(this, api));
        invClear.setTabCompleter(tabCompleter);

        PluginCommand enderClear = server.getPluginCommand("enderclear");
        enderClear.setExecutor(new EnderClearExecutor(this, api));
        enderClear.setTabCompleter(tabCompleter);
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
                return ispp.offlinePlayerSupport();
            } else if ("same_as_InvseeAPI".equalsIgnoreCase(s)) {
                return api.offlinePlayerSupport();
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

    //TODO configuration option to also clear out of the queue created by InvSee++_Give ?
    //TODO when enabled, the InvSee++_Give queue could also be re-polled after clearing happens.

}
