package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.internal.view.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.Exempt;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.response.ImplementationFault;
import com.janboerman.invsee.spigot.api.response.InventoryNotCreated;
import com.janboerman.invsee.spigot.api.response.InventoryOpenEventCancelled;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.NotOpenedReason;
import com.janboerman.invsee.spigot.api.response.OfflineSupportDisabled;
import com.janboerman.invsee.spigot.api.response.OpenResponse;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.response.TargetDoesNotExist;
import com.janboerman.invsee.spigot.api.response.TargetHasExemptPermission;
import com.janboerman.invsee.spigot.api.response.UnknownTarget;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import com.janboerman.invsee.spigot.perworldinventory.ProfileId;
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

public class EnderseeCommandExecutor implements CommandExecutor {

    private final InvseePlusPlus plugin;

    public EnderseeCommandExecutor(InvseePlusPlus plugin) {
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
        try {
            uuid = UUID.fromString(playerNameOrUUID);
            isUuid = true;
        } catch (IllegalArgumentException e) {
            isUuid = false;
            uuid = null;
        }

        final InvseeAPI api = plugin.getApi();
        final Target target = isUuid ? Target.byUniqueId(uuid) : Target.byUsername(playerNameOrUUID);
        //TODO why not just: plugin.getEnderChestCreationOptions() ?
        final CreationOptions<EnderChestSlot> creationOptions = CreationOptions.defaultEnderInventory(plugin)
                .withTitle(plugin.getTitleForEnderChest())
                .withMirror(plugin.getEnderChestMirror())
                .withOfflinePlayerSupport(plugin.offlinePlayerSupport())
                .withUnknownPlayerSupport(plugin.unknownPlayerSupport())
                .withBypassExemptedPlayers(player.hasPermission(Exempt.BYPASS_EXEMPT_ENDERCHEST))
                .withLogOptions(plugin.getLogOptions())
                .withPlaceholderPalette(plugin.getPlaceholderPalette());

        CompletableFuture<SpectateResponse<EnderSpectatorInventory>> pwiFuture = null;

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
            pwiFuture = uuidFuture.thenCompose(optId -> {
                if (optId.isPresent()) {
                    UUID uniqueId = optId.get();
                    ProfileId profileId = new ProfileId(pwiApi.getHook(), pwiOptions, uniqueId);
                    CompletableFuture<String> userNameFuture = finalIsUuid
                            ? api.fetchUserName(uniqueId).thenApply(o -> o.orElse("InvSee++ Player")).exceptionally(t -> "InvSee++ Player")
                            : CompletableFuture.completedFuture(playerNameOrUUID);
                    return userNameFuture.thenCompose(playerName -> pwiApi.spectateEnderChest(uniqueId, playerName, creationOptions, profileId));
                } else {
                    return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetDoesNotExists(target)));
                }
            });
        }

        //TODO Multiverse-Inventories


        CompletableFuture<OpenResponse<EnderSpectatorInventoryView>> fut;

        if (pwiFuture != null) {
            fut = pwiFuture.thenApply(response -> response.isSuccess()
                    ? ((PerWorldInventorySeeApi) api).openEnderSpectatorInventory(player, response.getInventory(), creationOptions)
                    : OpenResponse.closed(NotOpenedReason.notCreated(response.getReason())));
        }

        //TODO else if (mviFuture != null) { ... }

        else {
            //No PWI argument - just continue with the regular method

            if (isUuid) {
                final UUID finalUuid = uuid;
                fut = api.fetchUserName(uuid).thenApply(o -> o.orElse("InvSee++ Player")).exceptionally(t -> "InvSee++ Player")
                        .thenCompose(userName -> api.spectateEnderChest(player, finalUuid, userName, creationOptions));
            } else {
                fut = api.spectateEnderChest(player, playerNameOrUUID, creationOptions);
            }
        }

        //Gracefully handle failure and faults
        fut.whenComplete((openResponse, throwable) -> {
            if (throwable != null) {
                player.sendMessage(ChatColor.RED + "An error occurred while trying to open " + playerNameOrUUID + "'s enderchest.");
                plugin.getLogger().log(Level.SEVERE, "Error while trying to create ender-chest spectator inventory", throwable);
            } else {
                if (!openResponse.isOpen()) {
                    NotOpenedReason notOpenedReason = openResponse.getReason();
                    if (notOpenedReason instanceof InventoryOpenEventCancelled) {
                        player.sendMessage(ChatColor.RED + "Another plugin prevented you from spectating " + playerNameOrUUID + "'s ender chest.");
                    } else if (notOpenedReason instanceof InventoryNotCreated) {
                        NotCreatedReason reason = ((InventoryNotCreated) notOpenedReason).getNotCreatedReason();
                        if (reason instanceof TargetDoesNotExist) {
                            player.sendMessage(ChatColor.RED + "Player " + playerNameOrUUID + " does not exist.");
                        } else if (reason instanceof UnknownTarget) {
                            player.sendMessage(ChatColor.RED + "Player " + playerNameOrUUID + " has not logged onto the server yet.");
                        } else if (reason instanceof TargetHasExemptPermission) {
                            player.sendMessage(ChatColor.RED + "Player " + playerNameOrUUID + " is exempted from being spectated.");
                        } else if (reason instanceof ImplementationFault) {
                            player.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + playerNameOrUUID + "'s enderchest.");
                        } else if (reason instanceof OfflineSupportDisabled) {
                            player.sendMessage(ChatColor.RED + "Spectating offline players' enderchests is disabled.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Could not create " + playerNameOrUUID + "'s enderchest for an unknown reason.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Could not open " + playerNameOrUUID + "'s enderchest for an unknown reason.");
                    }
                } //else: it opened successfully: nothing to do there!
            }
        });

        return true;
    }

}
