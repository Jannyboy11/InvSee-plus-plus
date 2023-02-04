package com.janboerman.invsee.spigot.api.response;

import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Objects;

public class InventoryOpenEventCancelled implements NotOpenedReason {

    @Deprecated(forRemoval = true, since = "0.19.0")
    static final InventoryOpenEventCancelled INSTANCE = new InventoryOpenEventCancelled(null);

    private final InventoryOpenEvent event;

    InventoryOpenEventCancelled(InventoryOpenEvent event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "Inventory open event was cancelled";
    }

    public InventoryOpenEvent getEvent() {
        return event;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof InventoryOpenEventCancelled)) return false;

        InventoryOpenEventCancelled that = (InventoryOpenEventCancelled) o;
        return Objects.equals(this.getEvent(), that.getEvent());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getEvent());
    }
}
