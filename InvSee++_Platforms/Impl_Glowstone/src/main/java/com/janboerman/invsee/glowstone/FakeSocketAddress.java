package com.janboerman.invsee.glowstone;

import java.net.SocketAddress;

class FakeSocketAddress extends SocketAddress {

    static final SocketAddress INSTANCE = new FakeSocketAddress();

    private FakeSocketAddress() {
    }

    @Override
    public String toString() {
        return "FakeSocketAddress";
    }

}
