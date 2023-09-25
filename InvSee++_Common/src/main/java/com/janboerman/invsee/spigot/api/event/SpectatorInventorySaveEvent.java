package com.janboerman.invsee.spigot.api.event;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when the contents of a {@link SpectatorInventory} is saved to the target's player data file.
 * <br>
 * Cancelling this event has the effect of the inventory data not getting saved.
 */
public class SpectatorInventorySaveEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel;
    private final SpectatorInventory<?> spectatorInventory;

    public SpectatorInventorySaveEvent(SpectatorInventory<?> spectatorInventory) {
        this.spectatorInventory = spectatorInventory;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /** Get the spectator inventory. */
    public SpectatorInventory<?> getInventory() {
        return spectatorInventory;
    }

    /** Check whether saving is disabled. */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /** Set whether saving should be disabled. */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
