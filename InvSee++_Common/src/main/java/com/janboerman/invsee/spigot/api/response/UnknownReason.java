package com.janboerman.invsee.spigot.api.response;

/** We don't know why the SpectatorInventory could not be created or opened. */
public class UnknownReason implements NotCreatedReason, NotOpenedReason {

    static final UnknownReason INSTANCE = new UnknownReason();

    private UnknownReason() {}

    public String toString() {
        return "Unknown";
    }

}
