package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.response.*;
import com.janboerman.invsee.spigot.api.target.Target;
/*
import com.janboerman.invsee.spigot.multiverseinventories.MultiverseInventoriesSeeApi;
import com.janboerman.invsee.spigot.multiverseinventories.MviCommandArgs;
 */
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import com.janboerman.invsee.spigot.perworldinventory.PwiCommandArgs;
import com.janboerman.invsee.utils.Either;
import com.janboerman.invsee.utils.StringHelper;
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
        boolean isUuid;
        UUID uuid = null;
        try {
            uuid = UUID.fromString(playerNameOrUUID);
            isUuid = true;
        } catch (IllegalArgumentException e) {
            isUuid = false;
        }

        final InvseeAPI api = plugin.getApi();
        CompletableFuture<SpectateResponse<MainSpectatorInventory>> future = null;
        final String title = plugin.getTitleForInventory(isUuid ? Target.byUniqueId(uuid) : Target.byUsername(playerNameOrUUID));

        if (args.length > 1 && api instanceof PerWorldInventorySeeApi) {
            String pwiArgument = StringHelper.joinArray(" ", 1, args);
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
                    var profileId = new com.janboerman.invsee.spigot.perworldinventory.ProfileId(pwiApi.getHook(), pwiOptions, uniqueId);
                    CompletableFuture<String> userNameFuture = finalIsUuid
                            ? api.fetchUserName(uniqueId).thenApply(o -> o.orElse("InvSee++ Player")).exceptionally(t -> "InvSee++ Player")
                            : CompletableFuture.completedFuture(playerNameOrUUID);
                    return userNameFuture.thenCompose(playerName -> pwiApi.spectateInventory(uniqueId, playerName, title, profileId));
                } else {
                    return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetDoesNotExists(Target.byUsername(playerNameOrUUID))));
                }
            });
        }

        /*
        else if (args.length > 1 && api instanceof MultiverseInventoriesSeeApi) {
            String mviArgument = StringHelper.joinArray(" ", 1, args);
            MultiverseInventoriesSeeApi mviApi = (MultiverseInventoriesSeeApi) api;

            Either<String, MviCommandArgs> either = MviCommandArgs.parse(mviArgument, mviApi.getHook());
            if (either.isLeft()) {
                player.sendMessage(ChatColor.RED + either.getLeft());
                return true;
            }

            MviCommandArgs mviOptions = either.getRight();
            CompletableFuture<Optional<UUID>> uuidFuture = isUuid
                    ? CompletableFuture.completedFuture(Optional.of(uuid))
                    : mviApi.fetchUniqueId(playerNameOrUUID);

            final boolean finalIsUuid = isUuid;
            future = uuidFuture.thenCompose(optId -> {
                if (optId.isPresent()) {
                    UUID uniqueId = optId.get();
                    CompletableFuture<String> usernameFuture = finalIsUuid
                            ? api.fetchUserName(uniqueId).thenApply(o -> o.orElse("InvSee++ Player")).exceptionally(t -> "InvSee++ Player")
                            : CompletableFuture.completedFuture(playerNameOrUUID);
                    return usernameFuture.thenCompose(playerName -> mviApi.spectateInventory(uniqueId, playerName, title,
                            new com.janboerman.invsee.spigot.multiverseinventories.ProfileId(mviApi.getHook(), mviOptions, uniqueId, playerName)));
                } else {
                    return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetDoesNotExists(Target.byUsername(playerNameOrUUID))));
                }
            });
        }
         */

        if (future == null) { //TODO get rid of this. actually extend pwi api to also accept mirrors.
            //No PWI argument - just continue with the regular method
            if (isUuid) {
                //playerNameOrUUID is a UUID.
                final UUID finalUuid = uuid;

                api.fetchUserName(uuid).thenApply(o -> o.orElse("InvSee++ Player")).exceptionally(t -> "InvSee++ Player")
                        .thenAccept(userName -> api.spectateInventory(player, finalUuid, userName, title, plugin.offlinePlayerSupport(), Mirror.forInventory(plugin.getInventoryTemplate())));
            } else {
                api.spectateInventory(player, playerNameOrUUID, title, plugin.offlinePlayerSupport(), Mirror.forInventory(plugin.getInventoryTemplate()));
            }
        }

        return true;
    }

}
