package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.addon.give.common.GiveApi;
import com.janboerman.invsee.spigot.internal.MappingsVersion;
import org.bukkit.Server;

interface Setup {

    GiveApi getGiveApi();

    static Setup setup(GivePlugin plugin) {
        Server server = plugin.getServer();
        String serverClassName = server.getClass().getName();

        if ("org.bukkit.craftbukkit.v1_12_R1.CraftServer".equals(serverClassName)) {
            return new Impl_1_12_2();
        } else if ("org.bukkit.craftbukkit.v1_15_R1.CraftServer".equals(serverClassName)) {
            return new Impl_1_15_2();
        } else if ("org.bukkit.craftbukkit.v1_16_R3.CraftServer".equals(serverClassName)) {
            return new Impl_1_16_5();
        } else if ("org.bukkit.craftbukkit.v1_17_R1.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_17_1:
                    return new Impl_1_17_1();
            }
        } else if ("org.bukkit.craftbukkit.v1_18_R2.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_18_2:
                    return new Impl_1_18_2();
            }
        } else if ("org.bukkit.craftbukkit.v1_19_R1.CraftServer".equals(serverClassName)) {
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

class Impl_1_18_2 extends SetupImpl {
    Impl_1_18_2() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_18_2_R2.GiveImpl.INSTANCE);
    }
}

class Impl_1_17_1 extends SetupImpl {
    Impl_1_17_1() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_17_1_R1.GiveImpl.INSTANCE);
    }
}

class Impl_1_16_5 extends SetupImpl {
    Impl_1_16_5() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_16_R3.GiveImpl.INSTANCE);
    }
}

class Impl_1_15_2 extends SetupImpl {
    Impl_1_15_2() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_15_R1.GiveImpl.INSTANCE);
    }
}

class Impl_1_12_2 extends SetupImpl {
    Impl_1_12_2() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_12_R1.GiveImpl.INSTANCE);
    }
}