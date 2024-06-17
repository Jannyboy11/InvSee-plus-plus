package com.janboerman.invsee.spigot.internal.view;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an open window for a {@link MainSpectatorInventory}.
 */
public abstract class MainSpectatorInventoryView extends SpectatorInventoryView<PlayerInventorySlot> {

    protected MainSpectatorInventoryView(CreationOptions<PlayerInventorySlot> creationOptions) {
        super(creationOptions);
    }

    /** {@inheritDoc} */
    @Override
    public abstract MainSpectatorInventory getTopInventory();

    /** Sets the item in the desired slot of the InventoryView. If the slot is in the top inventory, then the inventory slot will be computed using
     * {@code getMirror().getSlot(slot).defaultIndex()}.
     * @see com.janboerman.invsee.spigot.api.template.Mirror */
    @Override
    public void setItem(int slot, ItemStack item) {
        if (0 <= slot && slot < getTopInventory().getSize()) {
            PlayerInventorySlot piSlot = getMirror().getSlot(slot);
            if (piSlot != null) super.setItem(piSlot.defaultIndex(), item);
        } else {
            super.setItem(slot, item);
        }
    }

    /** Gets the item in the desired slot of the InventoryView. If the slot is in the top inventory, then the inventory slot will be computed using
     * {@code getMirror().getSlot(slot).defaultIndex()}.
     * @see com.janboerman.invsee.spigot.api.template.Mirror */
    @Override
    public ItemStack getItem(int slot) {
        if (0 <= slot && slot < getTopInventory().getSize()) {
            PlayerInventorySlot piSlot = getMirror().getSlot(slot);
            return piSlot == null ? null : super.getItem(piSlot.defaultIndex());
        } else {
            return super.getItem(slot);
        }
    }

    /** {@inheritDoc} */
    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

}
