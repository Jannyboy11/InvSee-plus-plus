package com.janboerman.invsee.spigot.multiverseinventories;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;


public class MultiverseInventoriesSeeApi extends InvseeAPI {

    private final InvseeAPI wrapped;
    private final MultiverseInventoriesHook mviHook;

    public MultiverseInventoriesSeeApi(Plugin plugin, InvseeAPI wrapped, MultiverseInventoriesHook mviHook) {
        super(plugin);
        this.wrapped = Objects.requireNonNull(wrapped);
        this.mviHook = Objects.requireNonNull(mviHook);
    }


    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        return wrapped.spectateInventory(player, title);
    }

    @Override
    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title) {
        //TODO decide whether to load from vanilla save file, or MVI save file
        return wrapped.createOfflineInventory(playerId, playerName, title);
    }

    @Override
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory) {
        //TODO decide whether to save to MVI save file or not
        return wrapped.saveInventory(inventory);
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        return wrapped.spectateEnderChest(player, title);
    }

    @Override
    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title) {
        //TODO decide whether to load from vanilla save file, or MVI save file
        return wrapped.createOfflineEnderChest(playerId, playerName, title);
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest) {
        //TODO decide whether to save to MVI save file or not
        return wrapped.saveEnderChest(enderChest);
    }

}
