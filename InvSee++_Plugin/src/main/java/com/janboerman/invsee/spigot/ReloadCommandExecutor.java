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
        reloadPlugin(plugin);

        PluginManager pluginManager = plugin.getServer().getPluginManager();

        reloadPlugin(pluginManager, "InvSeePlusPlus_Give");
        reloadPlugin(pluginManager, "InvSeePlusPlus_Clear");
        reloadPlugin(pluginManager, "InvSeePlusPlus_Clone");

        sender.sendMessage(ChatColor.GREEN + "InvSee++ configuration was reloaded.");
        return true;
    }

    private static void reloadPlugin(PluginManager pluginManager, String pluginName) {
        Plugin plugin = pluginManager.getPlugin(pluginName);
        if (plugin != null) {
            reloadPlugin(plugin);
        }
    }

    private static void reloadPlugin(Plugin plugin) {
        plugin.getLogger().info("Reloading configuration..");
        plugin.reloadConfig();
    }
}
