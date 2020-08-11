package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

class EnderseeCommandExecutor implements CommandExecutor {

    private final InvseePlusPlus plugin;

    EnderseeCommandExecutor(InvseePlusPlus plugin) {
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

        CompletableFuture<Optional<EnderSpectatorInventory>> future;
        try {
            UUID uuid = UUID.fromString(playerNameOrUUID);
            future = plugin.getApi().spectateEnderChest(uuid, "InvSee++ Player", playerNameOrUUID + "'s enderchest");
        } catch (IllegalArgumentException e) {
            future = plugin.getApi().spectateEnderChest(playerNameOrUUID, playerNameOrUUID + "'s enderchest");
        }

        future.handle((optionalSpectatorInv, throwable) -> {
            if (throwable == null) {
                optionalSpectatorInv.ifPresentOrElse(player::openInventory, () -> player.sendMessage(ChatColor.RED + "Player " + playerNameOrUUID + " does not exist."));
            } else {
                player.sendMessage(ChatColor.RED + "An error occurred while trying to open " + playerNameOrUUID + "'s enderchest.");
                plugin.getLogger().log(Level.SEVERE, "Error while trying to create ender-chest spectator inventory", throwable);
            }
            return null;
        });

        return true;
    }

}
