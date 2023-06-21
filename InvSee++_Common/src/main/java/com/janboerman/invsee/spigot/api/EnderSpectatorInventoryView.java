package com.janboerman.invsee.spigot.api;

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
     * {@code getMirror().getIndex(EnderChestSlot.byDefaultIndex(slot))}.
     * @see com.janboerman.invsee.spigot.api.template.Mirror */
    @Override
    public void setItem(int slot, ItemStack item) {
        if (slot == -999 || slot >= getTopInventory().getSize()) {
            super.setItem(slot, item);
        } else {
            Integer index = getMirror().getIndex(EnderChestSlot.byDefaultIndex(slot));
            if (index != null) super.setItem(index, item);
        }
    }

    /** Gets the item in the desired slot of the InventoryView. If the slot is in the top inventory, then the inventory slot will be computed using
     * {@code getMirror().getIndex(EnderChestSlot.byDefaultIndex(slot))}.
     * @see com.janboerman.invsee.spigot.api.template.Mirror */
    @Override
    public ItemStack getItem(int slot) {
        if (slot == -999 || slot >= getTopInventory().getSize()) {
            return super.getItem(slot);
        } else {
            Integer index = getMirror().getIndex(EnderChestSlot.byDefaultIndex(slot));
            return index == null ? null : super.getItem(index);
        }
    }

    /** {@inheritDoc} */
    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

}
