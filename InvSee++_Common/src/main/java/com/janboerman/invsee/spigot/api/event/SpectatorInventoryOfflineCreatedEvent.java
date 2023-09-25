package com.janboerman.invsee.spigot.api.event;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Called when a {@link SpectatorInventory} is created whose target player is offline. */
public class SpectatorInventoryOfflineCreatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SpectatorInventory<?> spectatorInventory;

    public SpectatorInventoryOfflineCreatedEvent(SpectatorInventory<?> spectatorInventory) {
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

}
