package com.janboerman.invsee.spigot.impl_1_16;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InvseeImpl extends InvseeAPI {

    public InvseeImpl(Plugin plugin) {
        super(plugin);
        //TODO can I add an nms-implementation-specific version of OfflinePlayerStrategy that goes through the filesystem asynchronously?
    }

    @Override
    public CompletableFuture<Optional<SpectatorInventory>> createInventory(UUID player) {
        return null;
    }

    @Override
    public void saveInventory(SpectatorInventory inventory) {

    }

}
