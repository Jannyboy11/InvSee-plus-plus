package com.janboerman.invsee.spigot.addon.clone;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.PlayerTarget;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.target.UniqueIdTarget;
import com.janboerman.invsee.spigot.api.target.UsernameTarget;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class TargetUtil {

    private TargetUtil() {
    }

    static Target getTarget(String targetPlayer) {
        try {
            UUID uuid = UUID.fromString(targetPlayer);
            return Target.byUniqueId(uuid);
        } catch (IllegalArgumentException e) {
            return Target.byUsername(targetPlayer);
        }
    }

    static Target asTarget(CommandSender sender) {
        if (sender instanceof Player) {
            return Target.byPlayer((Player) sender);
        } else {
            return null;
        }
    }

    static Optional<Player> asPlayerExecutor(CommandSender sender) {
        if (sender instanceof Player) {
            return Optional.of((Player) sender);
        } else {
            return Optional.empty();
        }
    }

    static boolean isOnline(Server server, Target target) {
        if (target instanceof PlayerTarget) {
            return server.getPlayer(((PlayerTarget) target).getPlayer().getUniqueId()) != null;
        } else if (target instanceof UniqueIdTarget) {
            return server.getPlayer(((UniqueIdTarget) target).getUniqueId()) != null;
        } else if (target instanceof UsernameTarget) {
            return server.getPlayerExact(((UsernameTarget) target).getUsername()) != null;
        } else {
            return false;
        }
    }

    static CompletableFuture<SpectateResponse<MainSpectatorInventory>> getInventory(InvseeAPI api, Target target, Optional<Player> executor) {
        final CreationOptions<PlayerInventorySlot> creationOptions = executor.map(player -> api.mainInventoryCreationOptions(player)).orElseGet(() -> api.mainInventoryCreationOptions());
        if (target instanceof PlayerTarget) {
            return CompletableFuture.completedFuture(api.mainSpectatorInventory(((PlayerTarget) target).getPlayer(), creationOptions));
        } else if (target instanceof UniqueIdTarget) {
            UniqueIdTarget uuidTarget = (UniqueIdTarget) target;
            UUID uuid = uuidTarget.getUniqueId();
            CompletableFuture<String> username = api.fetchUserName(uuid).thenApply(o -> o.orElse("InvSee++ Player")).exceptionally(t -> "InvSee++ Player");
            return username.thenCompose(name -> api.mainSpectatorInventory(uuid, name, creationOptions));
        } else if (target instanceof UsernameTarget) {
            return api.mainSpectatorInventory(((UsernameTarget) target).getUsername(), creationOptions);
        } else {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.unknownTarget(target)));
        }
    }

    static CompletableFuture<SpectateResponse<EnderSpectatorInventory>> getEnderChest(InvseeAPI api, Target target, Optional<Player> executor) {
        final CreationOptions<EnderChestSlot> creationOptions = executor.map(player -> api.enderInventoryCreationOptions(player)).orElseGet(() -> api.enderInventoryCreationOptions());
        if (target instanceof PlayerTarget) {
            return CompletableFuture.completedFuture(api.enderSpectatorInventory(((PlayerTarget) target).getPlayer(), creationOptions));
        } else if (target instanceof UniqueIdTarget) {
            UniqueIdTarget uuidTarget = (UniqueIdTarget) target;
            UUID uuid = uuidTarget.getUniqueId();
            CompletableFuture<String> username = api.fetchUserName(uuid).thenApply(o -> o.orElse("InvSee++ Player")).exceptionally(t -> "InvSee++ Player");
            return username.thenCompose(name -> api.enderSpectatorInventory(uuid, name, creationOptions));
        } else if (target instanceof UsernameTarget) {
            return api.enderSpectatorInventory(((UsernameTarget) target).getUsername(), creationOptions);
        } else {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.unknownTarget(target)));
        }
    }
}
