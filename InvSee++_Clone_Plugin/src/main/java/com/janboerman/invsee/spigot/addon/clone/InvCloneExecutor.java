package com.janboerman.invsee.spigot.addon.clone;

import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.asPlayerExecutor;
import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.asTarget;
import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.getInventory;
import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.getTarget;
import static com.janboerman.invsee.spigot.addon.clone.TargetUtil.isOnline;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.Target;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

final class InvCloneExecutor implements CommandExecutor {

    private final ClonePlugin plugin;

    InvCloneExecutor(ClonePlugin plugin) {
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
        CompletableFuture<SpectateResponse<MainSpectatorInventory>>
                sourceFuture = getInventory(api, sourceTarget, asPlayerExecutor(sender)),
                targetFuture = getInventory(api, targetTarget, asPlayerExecutor(sender));

        CompletableFuture.allOf(sourceFuture, targetFuture)
                .whenCompleteAsync((__, throwable) -> {
                    if (throwable != null) {
                        plugin.getLogger().log(Level.SEVERE, "Error while trying to clone inventory.", throwable);
                        sender.sendMessage(ChatColor.RED + "Could not clone inventory for an unknown reason.");
                        return;
                    }
                    SpectateResponse<MainSpectatorInventory>
                            sourceResponse = sourceFuture.join(),
                            targetResponse = targetFuture.join();
                    if (!sourceResponse.isSuccess()) {
                        Responses.sendInventoryError(sender, sourceTarget, sourceResponse.getReason());
                    } else if (!targetResponse.isSuccess()) {
                        Responses.sendInventoryError(sender, targetTarget, targetResponse.getReason());
                    } else {
                        MainSpectatorInventory source = sourceResponse.getInventory();
                        MainSpectatorInventory target = targetResponse.getInventory();
                        target.setContents(source);
                        if (!isOnline(plugin.getServer(), targetTarget)) {
                            api.saveInventory(target)
                                    .whenComplete((___, ex) -> {
                                        plugin.getLogger().log(Level.SEVERE, "Error while trying to clone inventory.", ex);
                                        sender.sendMessage(ChatColor.RED + "Could not save contents of target inventory.");
                                    });
                        }
                        sender.sendMessage(ChatColor.GREEN + "Cloned " + sourceTarget + "'s inventory to " + targetTarget + ".");
                    }
                }, api.getScheduler()::executeSyncGlobal);

        return true;
    }
}
