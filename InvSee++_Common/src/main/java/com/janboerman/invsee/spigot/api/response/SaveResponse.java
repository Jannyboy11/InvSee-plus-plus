package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventory;

/** Result of saving a {@linkplain SpectatorInventory} using
 * {@link com.janboerman.invsee.spigot.api.InvseeAPI#saveInventory(MainSpectatorInventory)}
 * or {@link com.janboerman.invsee.spigot.api.InvseeAPI#saveEnderChest(EnderSpectatorInventory)}.
 */
public interface SaveResponse {

    /**
     * Get whether the spectator inventory was saved successfully.
     *
     * @return true if successful, otherwise false
     */
    public boolean isSaved();

    /**
     * Get the spectator inventory to be saved.
     *
     * @return the spectator inventory
     */
    public SpectatorInventory<?> getInventory();

    /** internal api */
    public static SaveResponse saved(SpectatorInventory<?> inventory) {
        return new Saved(inventory);
    }

    /** internal api */
    public static SaveResponse notSaved(SpectatorInventory<?> inventory) {
        return new NotSaved(inventory);
    }
}

final class Saved implements SaveResponse {

    private final SpectatorInventory<?> inventory;

    Saved(SpectatorInventory<?> inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean isSaved() {
        return true;
    }

    @Override
    public SpectatorInventory<?> getInventory() {
        return inventory;
    }
}

final class NotSaved implements SaveResponse {

    private final SpectatorInventory<?> inventory;

    NotSaved(SpectatorInventory<?> inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean isSaved() {
        return false;
    }

    @Override
    public SpectatorInventory<?> getInventory() {
        return inventory;
    }
}