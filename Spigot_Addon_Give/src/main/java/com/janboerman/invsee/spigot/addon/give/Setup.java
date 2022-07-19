package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.addon.give.common.GiveApi;
import com.janboerman.invsee.spigot.internal.MappingsVersion;
import org.bukkit.Server;

interface Setup {

    GiveApi getGiveApi();

    static Setup setup(GivePlugin plugin) {
        Server server = plugin.getServer();
        String serverClassName = server.getClass().getName();

        //TODO older versions
        if ("org.bukkit.craftbukkit.v1_19_R1.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_19:
                    return new Impl_1_19();
            }
        }

        throw new RuntimeException("Unsupported server software. Please run on (a fork of) CraftBukkit.");
    }
}

class SetupImpl implements Setup {

    private final GiveApi api;

    SetupImpl(GiveApi api) {
        this.api = api;
    }

    @Override
    public GiveApi getGiveApi() {
        return api;
    }
}

class Impl_1_19 extends SetupImpl {

    Impl_1_19() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_19_R1.GiveImpl.INSTANCE);
    }

}