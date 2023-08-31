package com.janboerman.invsee.spigot.internal;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.event.SpectatorInventoryOfflineCreateEvent;
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
    public static SpectatorInventorySave callInventorySaveEvent(Server server, SpectatorInventory<?> spectatorInventory) {
        SpectatorInventorySave event = new SpectatorInventorySave(spectatorInventory);
        server.getPluginManager().callEvent(event);
        return event;
    }

    //TODO instead of SpectatorInventory, have CreationOptions, Player (spectator) and Target ?
    /**
     * Calls {@link SpectatorInventoryOfflineCreateEvent}.
     *
     * @param server the server
     * @param spectatorInventory the inventory to b
     * @return the event
     */
    public static SpectatorInventoryOfflineCreateEvent callInventoryCreateEvent(Server server, SpectatorInventory<?> spectatorInventory) {
        SpectatorInventoryOfflineCreateEvent event = new SpectatorInventoryOfflineCreateEvent(spectatorInventory);
        server.getPluginManager().callEvent(event);
        return event;
    }
}
