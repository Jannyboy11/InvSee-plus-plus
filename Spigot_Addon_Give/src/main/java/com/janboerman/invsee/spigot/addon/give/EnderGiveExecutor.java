package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.response.ImplementationFault;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.OfflineSupportDisabled;
import com.janboerman.invsee.spigot.api.response.TargetDoesNotExist;
import com.janboerman.invsee.spigot.api.response.TargetHasExemptPermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EnderGiveExecutor implements CommandExecutor {

    private final GivePlugin plugin;
    private final InvseeAPI api;

    EnderGiveExecutor(GivePlugin plugin, InvseeAPI api) {
        this.plugin = plugin;
        this.api = api;
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

        uuidFuture.<Optional<String>, Void>thenCombineAsync(userNameFuture, (optUuid, optName) -> {
            if (optName.isEmpty() || optUuid.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Unknown player: " + inputPlayer);
            } else {
                String userName = optName.get();
                UUID uuid = optUuid.get();

                var responseFuture = api.enderSpectatorInventory(uuid, userName, userName + "s inventory");
                responseFuture.thenAcceptAsync(response -> {
                    if (response.isSuccess()) {
                        EnderSpectatorInventory inventory = response.getInventory();
                        Map<Integer, ItemStack> map = inventory.addItem(items);
                        if (map.isEmpty()) {
                            //success!!
                            if (plugin.getServer().getPlayer(uuid) != null)
                                //if the player is offline, save the inventory.
                                api.saveEnderChest(inventory);
                        } else {
                            //no success. for all the un-merged items, find an item in the player's inventory, and just exceed the max stack size!
                            int remainder = amount - map.get(0).getAmount();
                            boolean fallbackSuccess = false;
                            for (int idx = 0; idx < inventory.getSize(); idx++) {
                                ItemStack existingItem = inventory.getItem(idx);
                                if (existingItem.isSimilar(items)) {
                                    existingItem.setAmount(existingItem.getAmount() + remainder);
                                    fallbackSuccess = true;
                                    break;
                                }
                            }

                            if (!fallbackSuccess) {
                                items.setAmount(remainder);
                                sender.sendMessage(ChatColor.RED + "Could not add the following items to the player's inventory: " + items);
                                //TODO queue the items to be inserted again once possible?
                            }

                            if (plugin.getServer().getPlayer(uuid) != null && plugin.savePartialInventories())
                                api.saveEnderChest(inventory);
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
                            sender.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + implementationFault.getTarget() + "'s enderchest.");
                        } else if (reason instanceof OfflineSupportDisabled) {
                            sender.sendMessage(ChatColor.RED + "Spectating offline players' enderchest is disabled.");
                        }
                    }
                }, api.serverThreadExecutor);
            }

            return null;
        }, api.serverThreadExecutor);

        return true;
    }

}
