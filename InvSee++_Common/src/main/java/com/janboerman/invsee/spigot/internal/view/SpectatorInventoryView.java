package com.janboerman.invsee.spigot.internal.view;

import com.google.common.base.Preconditions;
import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;

import javax.annotation.Nullable;
import java.util.Objects;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an open window for a {@link SpectatorInventory}.
 * @param <Slot> the inventory's slot type.
 */
public abstract class SpectatorInventoryView<Slot> implements com.janboerman.invsee.spigot.api.SpectatorInventoryView<Slot> {

    private final CreationOptions<Slot> creationOptions;
    protected Target target;

    protected SpectatorInventoryView(CreationOptions<Slot> creationOptions) {
        this.creationOptions = Objects.requireNonNull(creationOptions, "creation options cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    public abstract SpectatorInventory<Slot> getTopInventory();

    /**
     * Get the difference tracked by this window.
     * @return the difference
     */
    public abstract @Nullable Difference getTrackedDifference();

    /**
     * Get the options this window was created with.
     * @return a copy of the creation options
     */
    public CreationOptions<Slot> getCreationOptions() {
        return creationOptions.clone();
    }

    //can't override getTitle because that does not work on 1.12 (the method is final :/)
    //can't override title() either because of Adventure Text (because I can't have return type String)
    //ooh the burden of supporting multiple versions!

    /**
     * Get the mirror the {@link SpectatorInventory} is viewed through.
     * @return the mirror
     * @see <a href="https://github.com/Jannyboy11/InvSee-plus-plus/wiki/Customising-spectator-inventories#mirror">Mirror explanation in the InvSee++ wiki</a>
     */
    public Mirror<Slot> getMirror() {
        return creationOptions.getMirror();
    }

    /**
     * Get the target of the {@link SpectatorInventory}.
     * @return the target
     */
    public Target getTarget() {
        SpectatorInventory<Slot> top = getTopInventory();
        return target == null ? target = Target.byGameProfile(top.getSpectatedPlayerId(), top.getSpectatedPlayerName()) : target;
    }

    @Override
    public void setItem(final int slot, @org.jetbrains.annotations.Nullable final ItemStack item) {
        Inventory inventory = getInventory(slot);
        if (inventory != null) {
            inventory.setItem(convertSlot(slot), item);
        } else if (item != null) {
            getPlayer().getWorld().dropItemNaturally(getPlayer().getLocation(), item);
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public ItemStack getItem(final int slot) {
        Inventory inventory = getInventory(slot);
        return (inventory == null) ? null : inventory.getItem(convertSlot(slot));
    }

    @Override
    public void setCursor(@org.jetbrains.annotations.Nullable final ItemStack item) {
        getPlayer().setItemOnCursor(item);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public ItemStack getCursor() {
        return getPlayer().getItemOnCursor();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Inventory getInventory(final int rawSlot) {
        // Slot may be -1 if not properly detected due to client bug
        // e.g. dropping an item into part of the enchantment list section of an enchanting table
        if (rawSlot == OUTSIDE || rawSlot == -1) {
            return null;
        }
        Preconditions.checkArgument(rawSlot >= 0, "Negative, non outside slot %s", rawSlot);
        Preconditions.checkArgument(rawSlot < countSlots(), "Slot %s greater than inventory slot count", rawSlot);

        if (rawSlot < getTopInventory().getSize()) {
            return getTopInventory();
        } else {
            return getBottomInventory();
        }
    }

    @Override
    public int convertSlot(final int rawSlot) {
        int numInTop = getTopInventory().getSize();
        // Index from the top inventory as having slots from [0,size]
        if (rawSlot < numInTop) {
            return rawSlot;
        }

        // Move down the slot index by the top size
        int slot = rawSlot - numInTop;

        // Player crafting slots are indexed differently. The matrix is caught by the first return.
        // Creative mode is the same, except that you can't see the crafting slots (but the IDs are still used)
        if (getType() == InventoryType.CRAFTING || getType() == InventoryType.CREATIVE) {
            /*
             * Raw Slots:
             *
             * 5             1  2     0
             * 6             3  4
             * 7
             * 8           45
             * 9  10 11 12 13 14 15 16 17
             * 18 19 20 21 22 23 24 25 26
             * 27 28 29 30 31 32 33 34 35
             * 36 37 38 39 40 41 42 43 44
             */

            /*
             * Converted Slots:
             *
             * 39             1  2     0
             * 38             3  4
             * 37
             * 36          40
             * 9  10 11 12 13 14 15 16 17
             * 18 19 20 21 22 23 24 25 26
             * 27 28 29 30 31 32 33 34 35
             * 0  1  2  3  4  5  6  7  8
             */

            if (slot < 4) {
                // Send [5,8] to [39,36]
                return 39 - slot;
            } else if (slot > 39) {
                // Slot lives in the extra slot section
                return slot;
            } else {
                // Reset index so 9 -> 0
                slot -= 4;
            }
        }

        // 27 = 36 - 9
        if (slot >= 27) {
            // Put into hotbar section
            slot -= 27;
        } else {
            // Take out of hotbar section
            // 9 = 36 - 27
            slot += 9;
        }

        return slot;
    }

    @NotNull
    @Override
    public InventoryType.SlotType getSlotType(final int slot) {
        InventoryType.SlotType type = InventoryType.SlotType.CONTAINER;
        if (slot >= 0 && slot < this.getTopInventory().getSize()) {
            switch (this.getType()) {
                case BLAST_FURNACE:
                case FURNACE:
                case SMOKER:
                    if (slot == 2) {
                        type = InventoryType.SlotType.RESULT;
                    } else if (slot == 1) {
                        type = InventoryType.SlotType.FUEL;
                    } else {
                        type = InventoryType.SlotType.CRAFTING;
                    }
                    break;
                case BREWING:
                    if (slot == 3) {
                        type = InventoryType.SlotType.FUEL;
                    } else {
                        type = InventoryType.SlotType.CRAFTING;
                    }
                    break;
                case ENCHANTING:
                    type = InventoryType.SlotType.CRAFTING;
                    break;
                case WORKBENCH:
                case CRAFTING:
                    if (slot == 0) {
                        type = InventoryType.SlotType.RESULT;
                    } else {
                        type = InventoryType.SlotType.CRAFTING;
                    }
                    break;
                case BEACON:
                    type = InventoryType.SlotType.CRAFTING;
                    break;
                case ANVIL:
                case SMITHING:
                case CARTOGRAPHY:
                case GRINDSTONE:
                case MERCHANT:
                    if (slot == 2) {
                        type = InventoryType.SlotType.RESULT;
                    } else {
                        type = InventoryType.SlotType.CRAFTING;
                    }
                    break;
                case STONECUTTER:
                    if (slot == 1) {
                        type = InventoryType.SlotType.RESULT;
                    } else {
                        type = InventoryType.SlotType.CRAFTING;
                    }
                    break;
                case LOOM:
                case SMITHING_NEW:
                    if (slot == 3) {
                        type = InventoryType.SlotType.RESULT;
                    } else {
                        type = InventoryType.SlotType.CRAFTING;
                    }
                    break;
                default:
                    // Nothing to do, it's a CONTAINER slot
            }
        } else {
            if (slot < 0) {
                type = InventoryType.SlotType.OUTSIDE;
            } else if (this.getType() == InventoryType.CRAFTING) { // Also includes creative inventory
                if (slot < 9) {
                    type = InventoryType.SlotType.ARMOR;
                } else if (slot > 35) {
                    type = InventoryType.SlotType.QUICKBAR;
                }
            } else if (slot >= (this.countSlots() - (9 + 4 + 1))) { // Quickbar, Armor, Offhand
                type = InventoryType.SlotType.QUICKBAR;
            }
        }
        return type;
    }

    @Override
    public void close() {
        getPlayer().closeInventory();
    }

    @Override
    public int countSlots() {
        return getTopInventory().getSize() + getBottomInventory().getSize();
    }

    @Override
    public boolean setProperty(@NotNull final InventoryView.Property prop, final int value) {
        return getPlayer().setWindowProperty(prop, value);
    }

}
