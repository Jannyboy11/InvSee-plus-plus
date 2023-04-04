package com.janboerman.invsee.spigot.api.response;

public class OfflineSupportDisabled implements NotCreatedReason {

    static final OfflineSupportDisabled INSTANCE = new OfflineSupportDisabled();

    private OfflineSupportDisabled() {}

    @Override
    public String toString() {
        return "Offline support disabled";
    }
}
