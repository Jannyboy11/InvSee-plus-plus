package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InvseeCommandExecutor implements CommandExecutor {

    private final InvseePlusPlus plugin;

    InvseeCommandExecutor(InvseePlusPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        String playerNameOrUUID = args[0];
        CompletableFuture<Optional<SpectatorInventory>> future;
        if (playerNameOrUUID.length() > 16) {
            UUID uuid = UUID.fromString(playerNameOrUUID);
            future = plugin.getApi().createInventory(uuid);
        } else {
            future = plugin.getApi().createInventory(playerNameOrUUID);
        }

        future.handle((optionalSpectatorInv, throwable) -> {
            if (throwable == null) {
                optionalSpectatorInv.ifPresentOrElse(player::openInventory, () -> player.sendMessage(ChatColor.RED + "Player " + playerNameOrUUID + " does not exist."));
            } else {
                player.sendMessage(ChatColor.RED + "An error occured while trying to open " + playerNameOrUUID + "'s inventory.");
            }
            return null;
        });

        return true;
    }

}
