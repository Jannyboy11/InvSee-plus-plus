package com.janboerman.invsee.spigot.api.template;

import com.janboerman.invsee.spigot.internal.template.EnderChestMirror;
import com.janboerman.invsee.spigot.internal.template.PlayerInventoryMirror;

/**
 * Represents a mirror that an inventory is viewed through.
 * Mirrors provide the power change the positions of slots in the grid window.
 * Hence, Mirrors only affect how players see the {@link com.janboerman.invsee.spigot.api.SpectatorInventory},
 * they don't change the outcome of {@link com.janboerman.invsee.spigot.api.SpectatorInventory#getItem(int)} and related methods.
 *
 * @param <Slot> type of slot that this mirror works on
 *
 * @see <a href="https://github.com/Jannyboy11/InvSee-plus-plus/wiki/Customising-spectator-inventories#mirror">Mirror explanation on the InvSee++ wiki</a>
 */
public interface Mirror<Slot> {

    /** Gets the index for the grid in which the slot is displayed.
     * This method must satisfy the equation {@code Objects.equals(index, getIndex(getSlot(index)))}.s*/
    public Integer getIndex(Slot slot);

    /** Get the slot for the index at which the slot is displayed.
     * This method must satisfy the equation {@code Objects.equals(slot, getSlot(getIndex(slot)))}.*/
    public Slot getSlot(int index);

    /** The default mirror which assigns each slot to its default index. */
    public static Mirror<PlayerInventorySlot> defaultPlayerInventory() {
        return DefaultMirrors.DEFAULT_PLAYERINVENTORY_MIRROR;
    }

    /** The default mirror which assigns each slot to its default index. */
    public static Mirror<EnderChestSlot> defaultEnderChest() {
        return DefaultMirrors.DEFAULT_ENDERCHEST_MIRROR;
    }

    /** Convert a template into a Mirror. */
    public static Mirror<PlayerInventorySlot> forInventory(String template) {
        return PlayerInventoryMirror.ofTemplate(template);
    }

    /** Convert a template into a Mirror. */
    public static Mirror<EnderChestSlot> forEnderChest(String template) {
        return EnderChestMirror.ofTemplate(template);
    }

    /** Convert a Mirror into a template. */
    public static String toInventoryTemplate(Mirror<PlayerInventorySlot> mirror) {
        if (mirror == DefaultMirrors.DEFAULT_PLAYERINVENTORY_MIRROR) {
            return PlayerInventoryMirror.DEFAULT_TEMPLATE;
        } else {
            return PlayerInventoryMirror.toTemplate(mirror);
        }
    }

    /** Convert a Mirror into a template. */
    public static String toEnderChestTemplate(Mirror<EnderChestSlot> mirror) {
        if (mirror == DefaultMirrors.DEFAULT_ENDERCHEST_MIRROR) {
            return EnderChestMirror.DEFAULT_TEMPLATE;
        } else {
            return EnderChestMirror.toTemplate(mirror);
        }
    }
}

class DefaultMirrors {

    private DefaultMirrors() {}

    static final Mirror<PlayerInventorySlot> DEFAULT_PLAYERINVENTORY_MIRROR = new Mirror<>() {
        @Override public Integer getIndex(PlayerInventorySlot playerInventorySlot) {
            if (playerInventorySlot == null) return null;
            return playerInventorySlot.defaultIndex();
        }

        @Override public PlayerInventorySlot getSlot(int index) {
            return PlayerInventorySlot.byDefaultIndex(index);
        }
    };

    static final Mirror<EnderChestSlot> DEFAULT_ENDERCHEST_MIRROR = new Mirror<>() {
        @Override public Integer getIndex(EnderChestSlot enderChestSlot) {
            if (enderChestSlot == null) return null;
            return enderChestSlot.defaultIndex();
        }

        @Override public EnderChestSlot getSlot(int index) {
            return EnderChestSlot.byDefaultIndex(index);
        }
    };

}