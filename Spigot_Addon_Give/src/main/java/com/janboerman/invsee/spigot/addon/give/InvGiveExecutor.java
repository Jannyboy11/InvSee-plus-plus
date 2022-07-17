package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.response.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class InvGiveExecutor implements CommandExecutor {

    private final GivePlugin plugin;
    private final InvseeAPI api;
    private final ItemQueueManager queueManager;

    InvGiveExecutor(GivePlugin plugin, InvseeAPI api, ItemQueueManager queueManager) {
        this.plugin = plugin;
        this.api = api;
        this.queueManager = queueManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length < 2) return false;

        String inputPlayer = args[0];
        String inputItemType = args[1];

        var eitherPlayer = Convert.convertPlayer(inputPlayer);
        CompletableFuture<Optional<UUID>> uuidFuture;
        CompletableFuture<Optional<String>> userNameFuture;
        if (eitherPlayer.isLeft()) {
            UUID uuid = eitherPlayer.getLeft();
            uuidFuture = CompletableFuture.completedFuture(Optional.of(uuid));
            userNameFuture = api.fetchUserName(uuid);
        } else {
            assert eitherPlayer.isRight();
            String userName = eitherPlayer.getRight();
            userNameFuture = CompletableFuture.completedFuture(Optional.of(userName));
            uuidFuture = api.fetchUniqueId(userName);
        }

        var eitherMaterial = Convert.convertItemType(inputItemType);
        if (eitherMaterial.isLeft()) { sender.sendMessage(ChatColor.RED + eitherMaterial.getLeft()); return true; }
        assert eitherMaterial.isRight();
        Material material = eitherMaterial.getRight();

        int amount;
        if (args.length > 2) {
            String inputAmount = args[2];
            //var eitherItems = Convert.convertItems(material, inputAmount);
            var eitherItems = Convert.convertAmount(inputAmount);
            if (eitherItems.isLeft()) { sender.sendMessage(ChatColor.RED + eitherItems.getLeft()); return true; }
            assert eitherItems.isRight();
            amount = eitherItems.getRight();
        } else {
            amount = 1;
        }
        ItemStack items = new ItemStack(material, amount);

        //TODO args[3] NBT tag!
        //TODO can I use SNBT from https://github.com/Querz/NBT ? There seems to be a problem with booleans tho: https://github.com/Querz/NBT/issues/63)
        //TODO or maybe use: https://github.com/TheNullicorn/Nedit

        uuidFuture.<Optional<String>, Void>thenCombineAsync(userNameFuture, (optUuid, optName) -> {
            if (optName.isEmpty() || optUuid.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Unknown player: " + inputPlayer);
            } else {
                String userName = optName.get();
                UUID uuid = optUuid.get();

                var responseFuture = api.mainSpectatorInventory(uuid, userName, userName + "s inventory");
                responseFuture.thenAcceptAsync(response -> {
                    if (response.isSuccess()) {
                        MainSpectatorInventory inventory = response.getInventory();
                        //inventory.setMaxStackSize(Integer.MAX_VALUE);
                        final ItemStack originalItems = items.clone();
                        Map<Integer, ItemStack> map = inventory.addItem(items);
                        if (map.isEmpty()) {
                            //success!!
                            if (plugin.getServer().getPlayer(uuid) == null)
                                //if the player is offline, save the inventory.
                                api.saveInventory(inventory);
                            items.setAmount(amount);
                            sender.sendMessage(ChatColor.GREEN + "Added " + items + " to " + userName + "'s inventory!");
                        } else {
                            //no success. for all the un-merged items, find an item in the player's inventory, and just exceed the material's max stack size!
                            int remainder = map.get(0).getAmount();

                            items.setAmount(remainder);

                            if (plugin.queueRemainingItems()) {
                                sender.sendMessage(ChatColor.YELLOW + "Could not add the following items to the player's inventory: " + items + ", enqueuing..");
                                queueManager.enqueueInventory(uuid, plugin.savePartialInventories() ? items : originalItems);
                            } else {
                                sender.sendMessage(ChatColor.RED + "Could not add the following items to the player's inventory: " + items);
                            }

                            if (plugin.getServer().getPlayer(uuid) == null && plugin.savePartialInventories())
                                api.saveInventory(inventory);
                        }
                    } else {
                        NotCreatedReason reason = response.getReason();
                        if (reason instanceof TargetDoesNotExist) {
                            var targetDoesNotExist = (TargetDoesNotExist) reason;
                            sender.sendMessage(ChatColor.RED + "Player " + targetDoesNotExist.getTarget() + " does not exist.");
                        } else if (reason instanceof TargetHasExemptPermission) {
                            var targetHasExemptPermission = (TargetHasExemptPermission) reason;
                            sender.sendMessage(ChatColor.RED + "Player " + targetHasExemptPermission.getTarget() + " is exempted from being spectated.");
                        } else if (reason instanceof ImplementationFault) {
                            var implementationFault = (ImplementationFault) reason;
                            sender.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + implementationFault.getTarget() + "'s inventory.");
                        } else if (reason instanceof OfflineSupportDisabled) {
                            sender.sendMessage(ChatColor.RED + "Spectating offline players' inventories is disabled.");
                        }
                    }
                }, api.serverThreadExecutor);
            }

            return null;
        }, api.serverThreadExecutor);

        return true;
    }

}
