package com.janboerman.invsee.spigot.api.response;

public interface NotOpenedReason {

    public static InventoryNotCreated notCreated(NotCreatedReason notCreatedReason) {
        return new InventoryNotCreated(notCreatedReason);
    }

    public static InventoryOpenEventCancelled inventoryOpenEventCancelled() {
        return InventoryOpenEventCancelled.INSTANCE;
    }

}
