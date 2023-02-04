package com.janboerman.invsee.spigot.api.response;

import org.bukkit.event.inventory.InventoryOpenEvent;

public interface NotOpenedReason {

    public static InventoryNotCreated notCreated(NotCreatedReason notCreatedReason) {
        return new InventoryNotCreated(notCreatedReason);
    }

    @Deprecated(forRemoval = true, since = "0.19.0")
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
