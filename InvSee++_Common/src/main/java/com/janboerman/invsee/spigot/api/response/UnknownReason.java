package com.janboerman.invsee.spigot.api.response;

public class UnknownReason implements NotCreatedReason, NotOpenedReason {

    static final UnknownReason INSTANCE = new UnknownReason();

    private UnknownReason() {}

    public String toString() {
        return "Unknown";
    }

}
