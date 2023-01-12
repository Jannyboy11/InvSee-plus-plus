package com.janboerman.invsee.spigot.api.response;

public class InventoryOpenEventCancelled implements NotOpenedReason {

    static final InventoryOpenEventCancelled INSTANCE = new InventoryOpenEventCancelled();

    private InventoryOpenEventCancelled() {
        //TODO actually wrap InventoryOpenEvent object.
    }

    @Override
    public String toString() {
        return "Inventory open event was cancelled";
    }
}
