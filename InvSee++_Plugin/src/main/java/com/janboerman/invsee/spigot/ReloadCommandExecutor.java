package com.janboerman.invsee.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
        sender.sendMessage(ChatColor.GREEN + "InvSee++ configuration was reloaded");
        return true;
    }
}
