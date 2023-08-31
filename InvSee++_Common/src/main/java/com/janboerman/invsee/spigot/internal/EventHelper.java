package com.janboerman.invsee.spigot.internal;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.event.SpectatorInventoryOfflineCreatedEvent;
import com.janboerman.invsee.spigot.api.event.SpectatorInventorySave;
import org.bukkit.Server;

public final class EventHelper {

    private EventHelper () {
    }

    /**
     * Calls {@link SpectatorInventorySave}.
     *
     * @param server the server
     * @param spectatorInventory the inventory to be saved
     * @return the event
     */
    public static SpectatorInventorySave callSpectatorInventorySaveEvent(Server server, SpectatorInventory<?> spectatorInventory) {
        SpectatorInventorySave event = new SpectatorInventorySave(spectatorInventory);
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
    public static void callSpectatorInventoryOfflineCreatedEvent(Server server, SpectatorInventory<?> spectatorInventory) {
        SpectatorInventoryOfflineCreatedEvent event = new SpectatorInventoryOfflineCreatedEvent(spectatorInventory);
        server.getPluginManager().callEvent(event);
    }

    //TODO implement callSpectatorInventoryOfflineCreateEvent with CreationOptions, Player (spectator) and Target.
}
