package com.janboerman.invsee.spigot.api.event;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

//TODO call this event, also implement SpectatorInventoryOfflineCreateEvent which is cancellable.
//TODO its fields will be: Player (spectator, nullable), CreationOptions, Target.
public class SpectatorInventoryOfflineCreatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel;

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

    public SpectatorInventory<?> getInventory() {
        return spectatorInventory;
    }

}
