package com.janboerman.invsee.test;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.InvseePlusPlus;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.TestingCompatLayer;
import com.janboerman.invsee.utils.FuzzyReflection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class InvseeTestPlugin extends JavaPlugin {

    private static final UUID JANNYBOY11_UUID = UUID.fromString("7940653f-a153-47e1-a184-c30a5075a2f5");
    private static final String JANNYBOY11_NAME = "Jannyboy11";

    private InvseePlusPlus invseePlusPlus;

    @Override
    public void onEnable() {
        this.invseePlusPlus = (InvseePlusPlus) getServer().getPluginManager().getPlugin("InvSeePlusPlus");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "Running tests...");
        runTests(sender);
        return true;
    }

    private void runTests(CommandSender sender) {
        InvseeAPI api = invseePlusPlus.getApi();

        testSavingInventoryWithoutChangesDoesNotChangeSaveFile(1, api)
                .whenComplete((success, error) -> {
                    if (error != null) {
                        sender.sendMessage(ChatColor.GREEN + "...Passed!");
                    } else {
                        error.printStackTrace();
                        sender.sendMessage("Oh noes! " + error.getMessage());
                    }
                });
    }

    private void logTestStep(int testNo, int testStep, String message) {
        getLogger().info("Test " + testNo + " step " + testStep + ". " + message);
    }

    private CompletableFuture<Void> testSavingInventoryWithoutChangesDoesNotChangeSaveFile(int testNo, InvseeAPI api) {
        // Scenario:
        //   1. load a player compound.
        //   2. load the same player's inventory.
        //   3. save the player's inventory again without edits.
        //   4. load the player's compound again.
        //   5. verify compounds from step 1 and 4 are equal!

        // 1.
        logTestStep(testNo, 1, "load a player compound.");
        var platform = (InvseePlatform & TestingCompatLayer) getPlatform(api);
        var originalTag = platform.loadPlayerSaveCompound(JANNYBOY11_UUID, JANNYBOY11_NAME);

        // 2.
        logTestStep(testNo, 2, "load the same player's inventory.");
        // normally in the main thread you'd never use Future#get or Future#join, but for testing it's fine.
        var future = api.mainSpectatorInventory(JANNYBOY11_UUID, JANNYBOY11_NAME);
        return future.thenCompose(response -> {
            var spectatorInventory = response.getInventory();
            // 3.
            logTestStep(testNo, 3, "save the player's inventory again without edits.");
            return api.saveInventory(spectatorInventory);
        }).thenApply(__ -> {
            // 4.
            logTestStep(testNo, 4, "load the player's compound again.");
            var newTag = platform.loadPlayerSaveCompound(JANNYBOY11_UUID, JANNYBOY11_NAME);

            // 5.
            logTestStep(testNo, 5, "verify compounds from step 1 and 4 are equal!");
            if (!originalTag.equals(newTag)) {
                throw new RuntimeException("Unequal player tags! original=" + originalTag + ", new=" + newTag);
            }

            return null;
        });
    }

    private static InvseePlatform getPlatform(InvseeAPI invseeAPI) {
        try {
            Field[] platformFields = FuzzyReflection.getFieldOfType(InvseeAPI.class, InvseePlatform.class);
            return (InvseePlatform) platformFields[0].get(invseeAPI);
        } catch (Exception e) {
            throw new RuntimeException("Could not obtain InvseePlatform", e);
        }
    }
}
