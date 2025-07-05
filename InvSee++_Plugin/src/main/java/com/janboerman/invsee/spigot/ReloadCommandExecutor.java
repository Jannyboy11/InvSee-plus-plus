package com.janboerman.invsee.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public final class ReloadCommandExecutor implements CommandExecutor {

    private final InvseePlusPlus plugin;

    ReloadCommandExecutor(InvseePlusPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        plugin.getLogger().info("Reloading configuration..");
        plugin.reloadConfig();

        PluginManager pluginManager = plugin.getServer().getPluginManager();

        Plugin invseePlusPlus_Give = pluginManager.getPlugin("InvSeePlusPlus_Give");
        if (invseePlusPlus_Give != null) {
            invseePlusPlus_Give.getLogger().info("Reloading configuration..");
            invseePlusPlus_Give.reloadConfig();
        }

        Plugin invseePlusPlus_Clear = pluginManager.getPlugin("InvSeePlusPlus_Clear");
        if (invseePlusPlus_Clear != null) {
            invseePlusPlus_Clear.getLogger().info("Reloading configuration..");
            invseePlusPlus_Clear.reloadConfig();
        }

        sender.sendMessage(ChatColor.GREEN + "InvSee++ configuration was reloaded");
        return true;
    }
}
