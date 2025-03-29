package com.janboerman.invsee.spigot.impl_1_21_5_R4;

import java.util.Objects;

import com.google.common.base.Preconditions;
import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventoryView;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

abstract class BukkitInventoryView<Slot> implements SpectatorInventoryView<Slot>, InventoryView {

    private final CreationOptions<Slot> creationOptions;
    protected Target target;

    BukkitInventoryView(CreationOptions<Slot> creationOptions) {
        this.creationOptions = Objects.requireNonNull(creationOptions, "creation options cannot be null");
    }

    @Override
    public void setCursor(ItemStack itemStack) {
        getPlayer().setItemOnCursor(itemStack);
    }

    @Override
    public ItemStack getCursor() {
        return getPlayer().getItemOnCursor();
    }

    @Override
    public Inventory getInventory(int rawSlot) {
        if (rawSlot == InventoryView.OUTSIDE || rawSlot == -1) {
            return null;
        } else {
            Preconditions.checkArgument(rawSlot >= 0, "Negative, non outside slot %s", rawSlot);
            Preconditions.checkArgument(rawSlot < this.countSlots(), "Slot %s greater than inventory slot count", rawSlot);
            return rawSlot < this.getTopInventory().getSize() ? this.getTopInventory() : this.getBottomInventory();
        }
    }

    @Override
    public int convertSlot(int rawSlot) {
        int topSize = getTopInventory().getSize();
        if (rawSlot < topSize) {
            return rawSlot;
        } else {
            int slot = rawSlot - topSize;
            if (slot >= 27) {
                slot -= 27;
            } else {
                slot += 9;
            }
            return slot;
        }
    }

    @Override
    public InventoryType.SlotType getSlotType(int slot) {
        if (slot < 0) {
            return InventoryType.SlotType.OUTSIDE;
        } else {
            int slotCount = countSlots();
            if (slotCount - 9 <= slot && slot < slotCount) {
                return InventoryType.SlotType.QUICKBAR;
            } else {
                return InventoryType.SlotType.CONTAINER;
            }
        }
    }

    @Override
    public void close() {
        getPlayer().closeInventory();
    }

    @Override
    public int countSlots() {
        return getTopInventory().getSize() + getBottomInventory().getStorageContents().length;
    }

    @Override
    public boolean setProperty(Property property, int value) {
        return getPlayer().setWindowProperty(property, value);
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
