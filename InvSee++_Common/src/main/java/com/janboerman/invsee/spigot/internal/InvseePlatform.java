package com.janboerman.invsee.spigot.internal;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.response.OpenResponse;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface InvseePlatform {

    //fields that are used by current InvseeImpl implementations can remain there for now. As a first step we can factor out the platform-specific creation methods!
    //problem is that Impl_1_8_R3 does reset the default Mirror (because Minecraft 1.8.8 doesn't have the offhand slot),
    //so I still think we do want to retain the ability to override default values for CreationOptions.

    //we do want to dependency-inject the Scheduler, I think. Or should we just pass the Scheduler as a parameter of every method that returns a CompletableFuture?
    //I might like the latter approach better.
    //TODO load InvSee++ as a Paper Plugin, that way we can name ourselves "InvSee++" instead of "InvSeePlusPlus" :)

    public abstract MainSpectatorInventory spectateInventory(HumanEntity target, CreationOptions<PlayerInventorySlot> options);

    public abstract CompletableFuture<SpectateResponse<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, CreationOptions<PlayerInventorySlot> options);

    public abstract CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory);

    public OpenResponse<MainSpectatorInventoryView> openMainSpectatorInventory(Player spectator, MainSpectatorInventory spectatorInventory, CreationOptions<PlayerInventorySlot> options);


    public abstract EnderSpectatorInventory spectateEnderChest(HumanEntity target, CreationOptions<EnderChestSlot> options);

    public abstract CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, CreationOptions<EnderChestSlot> options);

    public abstract CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest);

    public OpenResponse<EnderSpectatorInventoryView> openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory spectatorInventory, CreationOptions<EnderChestSlot> options);

}
