package com.janboerman.invsee.spigot.api.response;

import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Represents a reason why a {@link com.janboerman.invsee.spigot.api.SpectatorInventory} could not be opened.
 * @see OpenResponse
 */
public interface NotOpenedReason {

    public static InventoryNotCreated notCreated(NotCreatedReason notCreatedReason) {
        return new InventoryNotCreated(notCreatedReason);
    }

    @Deprecated//(forRemoval = true, since = "0.19.0") TODO remove in 1.0
    public static InventoryOpenEventCancelled inventoryOpenEventCancelled() {
        return InventoryOpenEventCancelled.INSTANCE;
    }

    public static InventoryOpenEventCancelled inventoryOpenEventCancelled(InventoryOpenEvent event) {
        return new InventoryOpenEventCancelled(event);
    }

    public static UnknownReason generic() {
        return UnknownReason.INSTANCE;
    }

}
