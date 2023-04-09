package com.janboerman.invsee.spigot.api.response;

import java.util.Objects;

/**
 * The {@link com.janboerman.invsee.spigot.api.SpectatorInventory} could not be opened, because the {@linkplain com.janboerman.invsee.spigot.api.SpectatorInventory} could not be created!
 */
public class InventoryNotCreated implements NotOpenedReason {

    private final NotCreatedReason notCreatedReason;

    InventoryNotCreated(NotCreatedReason notCreatedReason) {
        this.notCreatedReason = Objects.requireNonNull(notCreatedReason);
    }

    /**
     * Get why the {@link com.janboerman.invsee.spigot.api.SpectatorInventory} could not be created.
     * @return the reason
     */
    public NotCreatedReason getNotCreatedReason() {
        return notCreatedReason;
    }

    @Override
    public String toString() {
        return "Could not open inventory because of NotCreatedReason: " + notCreatedReason;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(notCreatedReason);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof InventoryNotCreated)) return false;

        InventoryNotCreated that = (InventoryNotCreated) o;
        return Objects.equals(this.getNotCreatedReason(), that.getNotCreatedReason());
    }

}
