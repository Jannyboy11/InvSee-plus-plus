package com.janboerman.invsee.spigot.multiverseinventories;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.response.OpenResponse;
import com.janboerman.invsee.spigot.api.response.SaveResponse;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import com.janboerman.invsee.spigot.api.Scheduler;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MultiverseInventoriesSeeApi extends InvseeAPI implements InvseePlatform {

    private final InvseePlatform wrapped;
    private final MultiverseInventoriesHook mviHook;

    private final Map<ProfileId, MainSpectatorInventory> inventories = new HashMap<>();
    private final Map<MainSpectatorInventory, Set<ProfileId>> inventoryKeys = new HashMap<>();  //a player profile can be matched by multiple profileKeys
    private final Map<ProfileId, EnderSpectatorInventory> enderchests = new HashMap<>();
    private final Map<EnderSpectatorInventory, Set<ProfileId>> enderchestKeys = new HashMap<>();   //a player profile can be matched by multiple profileKeys

    public MultiverseInventoriesSeeApi(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cachedInventories,
                                       InvseePlatform wrapped, MultiverseInventoriesHook mviHook) {
        super(plugin, null, lookup, scheduler, cachedInventories);

        this.wrapped = Objects.requireNonNull(wrapped);
        this.mviHook = Objects.requireNonNull(mviHook);
    }

    @Override
    protected InvseePlatform getPlatform() {
        return this;
    }

    public MultiverseInventoriesHook getHook() {
        return mviHook;
    }

    private void tie(ProfileId profileId, MainSpectatorInventory spectatorInventory) {
        assert !inventories.containsKey(profileId) || inventories.get(profileId).equals(spectatorInventory);
        inventories.put(profileId, spectatorInventory);
        inventoryKeys.computeIfAbsent(spectatorInventory, inv -> new HashSet<>()).add(profileId);
    }

    private void tie(ProfileId profileId, EnderSpectatorInventory spectatorInventory) {
        assert !enderchests.containsKey(profileId) || enderchests.get(profileId).equals(spectatorInventory);
        enderchests.put(profileId, spectatorInventory);
        enderchestKeys.computeIfAbsent(spectatorInventory, inv -> new HashSet<>()).add(profileId);
    }

    private void unTie(ProfileId profileId, MainSpectatorInventory spectatorInventory) {
        inventories.remove(profileId, spectatorInventory);
        Set<ProfileId> set = inventoryKeys.get(spectatorInventory);
        if (set != null) {
            set.remove(profileId);
            if (set.isEmpty()) inventoryKeys.remove(spectatorInventory);
        }
    }

    private void unTie(ProfileId profileId, EnderSpectatorInventory spectatorInventory) {
        enderchests.remove(profileId, spectatorInventory);
        Set<ProfileId> set = enderchestKeys.get(spectatorInventory);
        if (set != null) {
            set.remove(profileId);
            if (set.isEmpty()) enderchestKeys.remove(spectatorInventory);
        }
    }




    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> spectateInventory(UUID playerId, String playerName, String title, ProfileId profileId) {

        //TODO

        //TODO take into account that not all groups necessarily share inventories across worlds.
        //TODO take into account that multiverse-inventories manages storage contents, armour contents and offhand contents independently.
        //TODO THIS NEEDS TO BE TAKEN INTO ACCOUNT *ESPECIALLY* WHEN SAVING PLAYER DATA!

        Player onlinePlayer = plugin.getServer().getPlayer(playerId);
        if (onlinePlayer != null) {
            //TODO Set<ProfileKey> activeProfileKeys = mviHook.getActiveProfileKeys(HumanEntity player);


            //TODO more..
        }

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
    public OpenResponse<MainSpectatorInventoryView> openMainSpectatorInventory(Player spectator, MainSpectatorInventory spectatorInventory, CreationOptions<PlayerInventorySlot> options) {
        //TODO
        return wrapped.openMainSpectatorInventory(spectator, spectatorInventory, options);
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, CreationOptions<PlayerInventorySlot> options) {
        //TODO
        return wrapped.spectateInventory(player, options);
    }

    @Override
    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, CreationOptions<PlayerInventorySlot> options) {
        //TODO decide whether to load from vanilla save file, or MVI save file
        return wrapped.createOfflineInventory(playerId, playerName, options);
    }

    @Override
    public CompletableFuture<SaveResponse> saveInventory(MainSpectatorInventory inventory) {
        //TODO decide whether to save to MVI save file or not
        return wrapped.saveInventory(inventory);
    }

    @Override
    public OpenResponse<EnderSpectatorInventoryView> openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory spectatorInventory, CreationOptions<EnderChestSlot> options) {
        //TODO
        return wrapped.openEnderSpectatorInventory(spectator, spectatorInventory, options);
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, CreationOptions<EnderChestSlot> options) {
        //TODO
        return wrapped.spectateEnderChest(player, options);
    }

    @Override
    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, CreationOptions<EnderChestSlot> options) {
        //TODO decide whether to load from vanilla save file, or MVI save file
        return wrapped.createOfflineEnderChest(playerId, playerName, options);
    }

    @Override
    public CompletableFuture<SaveResponse> saveEnderChest(EnderSpectatorInventory enderChest) {
        //TODO decide whether to save to MVI save file or not
        return wrapped.saveEnderChest(enderChest);
    }

}
