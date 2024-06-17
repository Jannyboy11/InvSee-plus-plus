package com.janboerman.invsee.spigot.api;

import javax.annotation.Nullable;

import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;

/**
 * Represents an open window for a {@link SpectatorInventory}.
 * @param <Slot> the inventory's slot type.
 */
// cannot extend InventoryView, because this class was not an interface before Bukkit 1.21.
public interface SpectatorInventoryView<Slot> {

    /**
     * Get the top inventory of this view.
     * @return the top inventory
     */
    public  SpectatorInventory<Slot> getTopInventory();

    /**
     * Get the difference tracked by this window.
     * @return the difference
     */
    public @Nullable Difference getTrackedDifference();

    /**
     * Get the options this window was created with.
     * @return a copy of the creation options
     */
    public CreationOptions<Slot> getCreationOptions();

    /**
     * Get the title of this InventoryView.
     * @return the title
     */
    public String getTitle();

    /**
     * Get the mirror the {@link SpectatorInventory} is viewed through.
     * @return the mirror
     * @see <a href="https://github.com/Jannyboy11/InvSee-plus-plus/wiki/Customising-spectator-inventories#mirror">Mirror explanation in the InvSee++ wiki</a>
     */
    public Mirror<Slot> getMirror();

    /**
     * Get the target of the {@link SpectatorInventory}.
     * @return the target
     */
    public Target getTarget();

}
