package com.janboerman.invsee.spigot.multiverseinventories;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.janboerman.invsee.spigot.api.response.SpectateResponse;
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

    public MultiverseInventoriesHook getHook() {
        return mviHook;
    }

    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> spectateInventory(UUID playerId, String playerName, String title, ProfileId profileId) {

        //TODO

        /*  IF the player is online THEN
         *      IF the player's current profile (world, gamemode) matches the profileId THEN
         *          - open a live spectator inventory
         *          - tie the spectatorInventory and the profileId
         *      ELSE (player's profile doesn't match profileId) THEN
         *          - open an offline spectator inventory based on the profile identifier (use MVI storage)
         *          - tie the spectatorInventory and the profileId
         *  ELSE (the player is offline) THEN
         *      IF the player's logout profileKey (world, gamemode) matches the profileId THEN
         *          - open an offline spectator inventory based on the profile identifier
         *              - load from MVI storage if 'save_load_on_log_in_out' is true
         *              - load from Vanilla storage if 'save_load_on_log_in_out' is false
         *          - tie the spectatorInventory and profileId
         *      ELSE
         *          - open an offline spectator inventory based on the profile identifier (use MVI storage)
         *          - tie the spectatorInventory and the profileId
         */

        return null;
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
