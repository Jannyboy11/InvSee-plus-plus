package com.janboerman.invsee.spigot.internal.view;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an open window for an {@link EnderSpectatorInventory}.
 */
public abstract class EnderSpectatorInventoryView extends SpectatorInventoryView<EnderChestSlot> {

    protected EnderSpectatorInventoryView(CreationOptions<EnderChestSlot> creationOptions) {
        super(creationOptions);
    }

    /** {@inheritDoc} */
    @Override
    public abstract EnderSpectatorInventory getTopInventory();

    /** Sets the item in the desired slot of the InventoryView. If the slot is in the top inventory, then the inventory slot will be computed using
     * {@code getMirror().getSlot(slot).defaultIndex()}.
     * @see com.janboerman.invsee.spigot.api.template.Mirror */
    @Override
    public void setItem(int slot, ItemStack item) {
        if (slot == -999 || slot >= getTopInventory().getSize()) {
            super.setItem(slot, item);
        } else {
            EnderChestSlot ecSlot = getMirror().getSlot(slot);
            if (ecSlot != null) super.setItem(ecSlot.defaultIndex(), item);
        }
    }

    /** Gets the item in the desired slot of the InventoryView. If the slot is in the top inventory, then the inventory slot will be computed using
     * {@code getMirror().getSlot(slot).defaultIndex()}.
     * @see com.janboerman.invsee.spigot.api.template.Mirror */
    @Override
    public ItemStack getItem(int slot) {
        if (slot == -999 || slot >= getTopInventory().getSize()) {
            return super.getItem(slot);
        } else {
            EnderChestSlot ecSlot = getMirror().getSlot(slot);
            return ecSlot == null ? null : super.getItem(ecSlot.defaultIndex());
        }
    }

    /** {@inheritDoc} */
    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

}
