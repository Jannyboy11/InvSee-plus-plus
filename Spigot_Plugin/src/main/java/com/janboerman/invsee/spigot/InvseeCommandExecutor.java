package com.janboerman.invsee.spigot;

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

class InvseeCommandExecutor implements CommandExecutor {

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

        CompletableFuture<Optional<MainSpectatorInventory>> future;
        try {
            UUID uuid = UUID.fromString(playerNameOrUUID);
            future = plugin.getApi().spectateInventory(uuid, playerNameOrUUID + "'s inventory");
        } catch (IllegalArgumentException e) {
            future = plugin.getApi().spectateInventory(playerNameOrUUID, playerNameOrUUID + "'s inventory");
        }

        future.handle((optionalSpectatorInv, throwable) -> {
            if (throwable == null) {
                optionalSpectatorInv.ifPresentOrElse(player::openInventory, () -> player.sendMessage(ChatColor.RED + "Player " + playerNameOrUUID + " does not exist."));
            } else {
                player.sendMessage(ChatColor.RED + "An error occurred while trying to open " + playerNameOrUUID + "'s inventory.");
                plugin.getLogger().log(Level.SEVERE, "Error while trying to create main-inventory spectator inventory", throwable);
            }
            return null;
        });

        return true;
    }

    //TODO If we are on Paper, use AsyncTabCompleteEvent to implement async tabcompletion for offline players?
    //TODO I would need to adjust the InvSeeAPI interface because 'providing offline player names' can only be implemented efficiently by the serverversion-specific implementation class.

}
