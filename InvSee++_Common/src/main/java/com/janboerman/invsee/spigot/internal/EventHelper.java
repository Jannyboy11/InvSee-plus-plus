package com.janboerman.invsee.spigot.internal;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.event.SpectatorInventoryOfflineCreatedEvent;
import com.janboerman.invsee.spigot.api.event.SpectatorInventorySaveEvent;
import org.bukkit.Server;

public final class EventHelper {

    private EventHelper () {
    }

    /**
     * Calls {@link SpectatorInventorySaveEvent}.
     *
     * @param server the server
     * @param spectatorInventory the inventory to be saved
     * @return the event
     */
    public static SpectatorInventorySaveEvent callSpectatorInventorySaveEvent(Server server, SpectatorInventory<?> spectatorInventory) {
        SpectatorInventorySaveEvent event = new SpectatorInventorySaveEvent(spectatorInventory);
        server.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Calls {@link SpectatorInventoryOfflineCreatedEvent}.
     *
     * @param server the server
     * @param spectatorInventory the inventory to b
     * @return the event
     */
    public static <SI extends SpectatorInventory<?>> SI callSpectatorInventoryOfflineCreatedEvent(Server server, SI spectatorInventory) {
        SpectatorInventoryOfflineCreatedEvent event = new SpectatorInventoryOfflineCreatedEvent(spectatorInventory);
        server.getPluginManager().callEvent(event);
        return spectatorInventory;
    }

    //TODO implement callSpectatorInventoryOfflineCreateEvent with CreationOptions, Player (spectator) and Target.
    //TODO this SpectatorInventoryOfflineCreateEvent should implement the Cancellable interface too.
}
