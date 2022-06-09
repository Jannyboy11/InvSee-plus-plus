package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.response.ImplementationFault;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.OfflineSupportDisabled;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.response.TargetDoesNotExist;
import com.janboerman.invsee.spigot.api.response.TargetHasExemptPermission;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import com.janboerman.invsee.spigot.perworldinventory.ProfileId;
import com.janboerman.invsee.spigot.perworldinventory.PwiCommandArgs;
import com.janboerman.invsee.utils.Either;
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
        UUID uuid;
        boolean isUuid;
        CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future = null;
        try {
            uuid = UUID.fromString(playerNameOrUUID);
            isUuid = true;
        } catch (IllegalArgumentException e) {
            isUuid = false;
            uuid = null;
        }

        InvseeAPI api = plugin.getApi();
        if (args.length > 1 && api instanceof PerWorldInventorySeeApi) {
            String pwiArgument = args[1];
            PerWorldInventorySeeApi pwiApi = (PerWorldInventorySeeApi) api;

            Either<String, PwiCommandArgs> either = PwiCommandArgs.parse(pwiArgument, pwiApi.getHook());
            if (either.isLeft()) {
                player.sendMessage(ChatColor.RED + either.getLeft());
                return true;
            }

            PwiCommandArgs pwiOptions = either.getRight();
            CompletableFuture<Optional<UUID>> uuidFuture = isUuid
                    ? CompletableFuture.completedFuture(Optional.of(uuid))
                    : pwiApi.fetchUniqueId(playerNameOrUUID);

            final boolean finalIsUuid = isUuid;
            future = uuidFuture.thenCompose(optId -> {
                if (optId.isPresent()) {
                    UUID uniqueId = optId.get();
                    ProfileId profileId = new ProfileId(pwiApi.getHook(), pwiOptions, uniqueId);
                    CompletableFuture<String> userNameFuture = finalIsUuid
                            ? api.fetchUserName(uniqueId).thenApply(o -> o.orElse("InvSee++ Player")).exceptionally(t -> "InvSee++ Player")
                            : CompletableFuture.completedFuture(playerNameOrUUID);
                    return userNameFuture.thenCompose(playerName -> pwiApi.spectateEnderChest(uniqueId, playerName, playerName + "'s enderchest", profileId));
                } else {
                    return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetDoesNotExists(Target.byUsername(playerNameOrUUID))));
                }
            });
        }

        if (future == null) {
            //No PWI argument - just continue with the regular method
            if (isUuid) {
                final UUID finalUuid = uuid;
                future = api.fetchUserName(uuid).thenApply(o -> o.orElse("InvSee++ Player")).exceptionally(t -> "InvSee++ Player")
                        .thenCompose(userName -> api.enderSpectatorInventory(finalUuid, userName, playerNameOrUUID + "'s enderchest", plugin.offlinePlayerSupport()));
            } else {
                future = api.enderSpectatorInventory(playerNameOrUUID, playerNameOrUUID + "'s enderchest", plugin.offlinePlayerSupport());
            }
        }

        future.whenComplete((response, throwable) -> {
            if (throwable == null) {
                if (response.isSuccess()) {
                    player.openInventory(response.getInventory());
                } else {
                    NotCreatedReason reason = response.getReason();
                    if (reason instanceof TargetDoesNotExist) {
                        var targetDoesNotExist = (TargetDoesNotExist) reason;
                        player.sendMessage(ChatColor.RED + "Player " + targetDoesNotExist.getTarget() + " does not exist.");
                    } else if (reason instanceof TargetHasExemptPermission) {
                        var targetHasExemptPermission = (TargetHasExemptPermission) reason;
                        player.sendMessage(ChatColor.RED + "Player " + targetHasExemptPermission.getTarget() + " is exempted from being spectated.");
                    } else if (reason instanceof ImplementationFault) {
                        var implementationFault = (ImplementationFault) reason;
                        player.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + implementationFault.getTarget() + "'s enderchest.");
                    } else if (reason instanceof OfflineSupportDisabled) {
                        player.sendMessage(ChatColor.RED + "Spectating offline players' inventories is disabled.");
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "An error occurred while trying to open " + playerNameOrUUID + "'s enderchest.");
                plugin.getLogger().log(Level.SEVERE, "Error while trying to create ender-chest spectator inventory", throwable);
            }
        });

        return true;
    }

}
