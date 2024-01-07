package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.addon.give.common.GiveApi;
import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.response.*;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.utils.Either;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

class InvGiveExecutor implements CommandExecutor {

    private final GivePlugin plugin;
    private final InvseeAPI invseeApi;
    private final GiveApi giveApi;
    private final ItemQueueManager queueManager;

    InvGiveExecutor(GivePlugin plugin, InvseeAPI invseeApi, GiveApi giveApi, ItemQueueManager queueManager) {
        this.plugin = plugin;
        this.invseeApi = invseeApi;
        this.giveApi = giveApi;
        this.queueManager = queueManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length < 2) return false;

        String inputPlayer = args[0];
        String inputItemType = args[1];

        Either<UUID, String> eitherPlayer = Convert.convertPlayer(inputPlayer);
        CompletableFuture<Optional<UUID>> uuidFuture;
        CompletableFuture<Optional<String>> userNameFuture;
        if (eitherPlayer.isLeft()) {
            UUID uuid = eitherPlayer.getLeft();
            uuidFuture = CompletableFuture.completedFuture(Optional.of(uuid));
            userNameFuture = invseeApi.fetchUserName(uuid);
        } else {
            assert eitherPlayer.isRight();
            String userName = eitherPlayer.getRight();
            userNameFuture = CompletableFuture.completedFuture(Optional.of(userName));
            uuidFuture = invseeApi.fetchUniqueId(userName);
        }

        Either<String, ItemType> eitherMaterial = Convert.convertItemType(inputItemType);
        if (eitherMaterial.isLeft()) { sender.sendMessage(ChatColor.RED + eitherMaterial.getLeft()); return true; }
        assert eitherMaterial.isRight();
        ItemType itemType = eitherMaterial.getRight();

        int amount;
        if (args.length > 2) {
            String inputAmount = args[2];
            Either<String, Integer> eitherItems = Convert.convertAmount(inputAmount);
            if (eitherItems.isLeft()) { sender.sendMessage(ChatColor.RED + eitherItems.getLeft()); return true; }
            assert eitherItems.isRight();
            amount = eitherItems.getRight();
        } else {
            amount = 1;
        }

        ItemStack items = itemType.toItemStack(amount);

        if (args.length > 3) {
            StringJoiner inputTag = new StringJoiner(" ");
            for (int i = 3; i < args.length; i++) inputTag.add(args[i]);
            try {
                items = giveApi.applyTag(items, inputTag.toString());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + e.getMessage());
                return true;
            }
        }

        final ItemStack finalItems = items;
        final CreationOptions<PlayerInventorySlot> creationOptions = invseeApi.mainInventoryCreationOptions()
                .withOfflinePlayerSupport(plugin.offlinePlayerSupport())
                .withUnknownPlayerSupport(plugin.unknownPlayerSupport())
                .withBypassExemptedPlayers(plugin.bypassExemptInvsee(sender));

        uuidFuture.<Optional<String>, Void>thenCombineAsync(userNameFuture, (optUuid, optName) -> {
            if (!optName.isPresent() || !optUuid.isPresent()) {
                sender.sendMessage(ChatColor.RED + "Unknown player: " + inputPlayer);
            } else {
                String userName = optName.get();
                UUID uuid = optUuid.get();

                CompletableFuture<SpectateResponse<MainSpectatorInventory>> responseFuture =
                        invseeApi.mainSpectatorInventory(uuid, userName, creationOptions);
                responseFuture.thenAcceptAsync(response -> {
                    if (response.isSuccess()) {
                        MainSpectatorInventory inventory = response.getInventory();
                        final ItemStack originalItems = finalItems.clone();
                        Map<Integer, ItemStack> map = inventory.addItem(new ItemStack[] { finalItems });
                        if (map.isEmpty()) {
                            //success!!
                            if (plugin.getServer().getPlayer(uuid) == null)
                                //if the player is offline, save the inventory.
                                invseeApi.saveInventory(inventory).whenComplete((v, e) -> {
                                    if (e != null) plugin.getLogger().log(Level.SEVERE, "Could not save inventory", e);
                                });
                            sender.sendMessage(ChatColor.GREEN + "Added " + originalItems + " to " + userName + "'s inventory!");
                        } else {
                            //no success. for all the un-merged items, find an item in the player's inventory, and just exceed the material's max stack size!
                            int remainder = map.get(0).getAmount();

                            finalItems.setAmount(remainder);

                            if (plugin.queueRemainingItems()) {
                                sender.sendMessage(ChatColor.YELLOW + "Could not add the following items to the player's inventory: " + finalItems + ", enqueuing..");
                                queueManager.enqueueInventory(uuid, plugin.savePartialInventories() ? finalItems : originalItems);
                            } else {
                                sender.sendMessage(ChatColor.RED + "Could not add the following items to the player's inventory: " + finalItems);
                            }

                            if (plugin.getServer().getPlayer(uuid) == null && plugin.savePartialInventories())
                                invseeApi.saveInventory(inventory).whenComplete((v, e) -> {
                                    if (e != null) plugin.getLogger().log(Level.SEVERE, "Could not save inventory", e);
                                });
                        }
                    } else {
                        NotCreatedReason reason = response.getReason();
                        if (reason instanceof TargetDoesNotExist) {
                            TargetDoesNotExist targetDoesNotExist = (TargetDoesNotExist) reason;
                            sender.sendMessage(ChatColor.RED + "Player " + targetDoesNotExist.getTarget() + " does not exist.");
                        } else if (reason instanceof UnknownTarget) {
                            UnknownTarget unknownTarget = (UnknownTarget) reason;
                            sender.sendMessage(ChatColor.RED + "Player " + unknownTarget.getTarget() + " has not logged onto the server yet.");
                        } else if (reason instanceof TargetHasExemptPermission) {
                            TargetHasExemptPermission targetHasExemptPermission = (TargetHasExemptPermission) reason;
                            sender.sendMessage(ChatColor.RED + "Player " + targetHasExemptPermission.getTarget() + " is exempted from being spectated.");
                        } else if (reason instanceof ImplementationFault) {
                            ImplementationFault implementationFault = (ImplementationFault) reason;
                            sender.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + implementationFault.getTarget() + "'s inventory.");
                        } else if (reason instanceof OfflineSupportDisabled) {
                            sender.sendMessage(ChatColor.RED + "Spectating offline players' inventories is disabled.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Cannot give to " + inputPlayer + "'s inventory for an unknown reason.");
                        }
                    }
                }, runnable -> invseeApi.getScheduler().executeSyncPlayer(uuid, runnable, null));
            }

            return null;
        }, invseeApi.getScheduler()::executeSyncGlobal);

        return true;
    }

}
