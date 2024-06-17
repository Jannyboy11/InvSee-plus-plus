package com.janboerman.invsee.spigot.impl_1_12_R1;

import java.util.Objects;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventoryView;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;

import org.bukkit.inventory.InventoryView;

abstract class BukkitInventoryView<Slot> extends InventoryView implements SpectatorInventoryView<Slot> {

    private final CreationOptions<Slot> creationOptions;
    protected Target target;

    BukkitInventoryView(CreationOptions<Slot> creationOptions) {
        this.creationOptions = Objects.requireNonNull(creationOptions, "creation options cannot be null");
    }

    @Override
    public CreationOptions<Slot> getCreationOptions() {
        return creationOptions.clone();
    }

    @Override
    public Mirror<Slot> getMirror() {
        return creationOptions.getMirror();
    }

    @Override
    public Target getTarget() {
        SpectatorInventory<Slot> top = getTopInventory();
        return target == null ? target = Target.byGameProfile(top.getSpectatedPlayerId(), top.getSpectatedPlayerName()) : target;
    }

}
