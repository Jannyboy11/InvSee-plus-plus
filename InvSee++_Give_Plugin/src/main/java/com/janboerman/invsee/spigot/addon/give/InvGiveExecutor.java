package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.addon.give.cmd.ArgParser;
import com.janboerman.invsee.spigot.addon.give.cmd.ArgType;
import com.janboerman.invsee.spigot.addon.give.common.GiveApi;
import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.response.*;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.utils.Either;
import com.janboerman.invsee.utils.Pair;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        Optional<List<ArgType>> maybeFormat = ArgParser.determineFormat(args);
        if (!maybeFormat.isPresent()) {
            // Could not recognise the format. Exit early and let bukkit print the usage string.
            return false;
        }

        List<ArgType> format = maybeFormat.get();
        Map<ArgType, String> groupedArguments = ArgParser.splitArguments(format, args);

        String inputPlayer = groupedArguments.get(ArgType.TARGET);
        Pair<CompletableFuture<Optional<UUID>>, CompletableFuture<Optional<String>>> futures = ArgParser.parseTarget(invseeApi, inputPlayer);
        CompletableFuture<Optional<UUID>> uuidFuture = futures.getFirst();
        CompletableFuture<Optional<String>> userNameFuture = futures.getSecond();

        Either<String, ItemStack> eitherStack = ArgParser.parseItem(giveApi, groupedArguments);
        if (eitherStack.isLeft()) { sender.sendMessage(ChatColor.RED + eitherStack.getLeft()); return true; }
        assert eitherStack.isRight();

        final ItemStack finalItems = eitherStack.getRight();
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
