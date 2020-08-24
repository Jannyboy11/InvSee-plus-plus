package com.janboerman.invsee.spigot.perworldinventory;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InvseeImplPWI extends InvseeAPI {

    private final InvseeAPI wrapped;
    private final PerWorldInventoryHook pwiHook;

    public InvseeImplPWI(Plugin plugin, InvseeAPI wrapped, PerWorldInventoryHook pwiHook) {
        super(plugin);
        this.wrapped = Objects.requireNonNull(wrapped);
        this.pwiHook = Objects.requireNonNull(pwiHook);
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        return wrapped.spectateInventory(player, title);
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        return wrapped.spectateEnderChest(player, title);
    }

    @Override
    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title) {
        //pwiHook.getOrCreateProfile();
        //TODO call wrapper
        //TODO if there is a profile, transfer from profile to wrapper spectator inv
        //TODO (if the profile is null, then just don't transfer)
        //TODO profit!
        return null;
    }

    @Override
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory) {
        //TODO if the spectated player is managed by PWI (because its world is managed by PWI)
        //TODO then also save the goddam inventory to PWI's storage
        //TODO that can be done by loading the profile, applying the contents from the MainSpectatorInventory and saving it again

        return null;
    }

    @Override
    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title) {
        return null;
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest) {
        return null;
    }
}
