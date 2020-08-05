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
import java.util.logging.Level;

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
        //TODO support world argument?

        CompletableFuture<Optional<SpectatorInventory>> future;
        try {
            UUID uuid = UUID.fromString(playerNameOrUUID);
            future = plugin.getApi().spectate(uuid);
        } catch (IllegalArgumentException e) {
            future = plugin.getApi().spectate(playerNameOrUUID);
        }

        future.handle((optionalSpectatorInv, throwable) -> {
            if (throwable == null) {
                optionalSpectatorInv.ifPresentOrElse(player::openInventory, () -> player.sendMessage(ChatColor.RED + "Player " + playerNameOrUUID + " does not exist."));
            } else {
                player.sendMessage(ChatColor.RED + "An error occured while trying to open " + playerNameOrUUID + "'s inventory.");
                plugin.getLogger().log(Level.SEVERE, "Error while trying to create spectator inventory", throwable);
            }
            return null;
        });

        return true;
    }

    //TODO If we are on Paper, use AsyncTabCompleteEvent to implement async tabcompletion for offline players?
    //TODO I would need to adjust the InvSeeAPI interface because 'providing offline player names' can only be implemented efficiently by the serverversion-specific implementation class.
}
