package com.janboerman.invsee.spigot.addon.clone;

import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.asPlayerExecutor;
import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.asTarget;
import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.getEnderChest;
import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.getTarget;
import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.isOnline;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.Target;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

final class EnderCloneExecutor implements CommandExecutor {

    private final ClonePlugin plugin;

    EnderCloneExecutor(ClonePlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;

        Target sourceTarget = getTarget(args[0]);
        Target targetTarget = args.length > 1 ? getTarget(args[1]) : asTarget(sender);
        if (targetTarget == null) {
            sender.sendMessage(ChatColor.RED + "Either execute this command as a player, or provide a target player.");
            return true;
        }

        InvseeAPI api = plugin.getApi();
        CompletableFuture<SpectateResponse<EnderSpectatorInventory>>
                sourceFuture = getEnderChest(api, sourceTarget, asPlayerExecutor(sender)),
                targetFuture = getEnderChest(api, targetTarget, asPlayerExecutor(sender));

        CompletableFuture.allOf(sourceFuture, targetFuture)
                .whenCompleteAsync((__, throwable) -> {
                    if (throwable != null) {
                        plugin.getLogger().log(Level.SEVERE, "Error while trying to clone enderchest.", throwable);
                        sender.sendMessage(ChatColor.RED + "Could not clone enderchest for an unknown reason.");
                        return;
                    }
                    SpectateResponse<EnderSpectatorInventory>
                            sourceResponse = sourceFuture.join(),
                            targetResponse = targetFuture.join();
                    if (!sourceResponse.isSuccess()) {
                        Responses.sendEnderChestError(sender, sourceTarget, sourceResponse.getReason());
                    } else if (!targetResponse.isSuccess()) {
                        Responses.sendEnderChestError(sender, targetTarget, targetResponse.getReason());
                    } else {
                        EnderSpectatorInventory source = sourceResponse.getInventory();
                        EnderSpectatorInventory target = targetResponse.getInventory();
                        target.setContents(source);
                        if (!isOnline(plugin.getServer(), targetTarget)) {
                            api.saveEnderChest(target)
                                    .whenComplete((___, ex) -> {
                                        plugin.getLogger().log(Level.SEVERE, "Error while trying to clone enderchest.", ex);
                                        sender.sendMessage(ChatColor.RED + "Could not save contents of target enderchest.");
                                    });
                        }
                        sender.sendMessage(ChatColor.GREEN + "Cloned " + sourceTarget + "'s enderchest to " + targetTarget + ".");
                    }
                }, api.getScheduler()::executeSyncGlobal);

        return true;
    }
}
