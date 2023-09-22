package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.addon.give.common.GiveApi;
import com.janboerman.invsee.spigot.internal.MappingsVersion;
import org.bukkit.Server;

interface Setup {

    GiveApi getGiveApi();

    static Setup setup(GivePlugin plugin) {
        Server server = plugin.getServer();
        String serverClassName = server.getClass().getName();

        if ("org.bukkit.craftbukkit.v1_8_R3.CraftServer".equals(serverClassName)) {
            return new Impl_1_8_8();
        } else if ("org.bukkit.craftbukkit.v1_12_R1.CraftServer".equals(serverClassName)) {
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
        } else if ("org.bukkit.craftbukkit.v1_19_R3.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_19_4:
                    return new Impl_1_19_4();
            }
        } else if ("org.bukkit.craftbukkit.v1_20_R1.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_20_1:
                    return new Impl_1_20_1();
            }
        } else if ("org.bukkit.craftbukkit.v1_20_R2.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_20_2:
                    return new Impl_1_20_2();
            }
        } else if ("net.glowstone.GlowServer".equals(serverClassName)) {
            return new Impl_Glowstone();
        }

        if (server.getClass().getSimpleName().equals("CraftServer")) {
            throw new RuntimeException("Unsupported CraftBukkit version. Please run on one of [1.8.8, 1.12.2, 1.15.2, 1.16.5, 1.17.1, 1.18.2, 1.19.4, 1.20.1, 1.20.2]. Are you running the latest InvSee++_Give?");
        } else {
            throw new RuntimeException("Unsupported server software. Please run on (a fork of) CraftBukkit or Glowstone.");
        }
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

class Impl_1_20_2 extends SetupImpl {
    Impl_1_20_2() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_20_2_R2.GiveImpl.INSTANCE);
    }
}

class Impl_1_20_1 extends SetupImpl {
    Impl_1_20_1() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_20_1_R1.GiveImpl.INSTANCE);
    }
}

class Impl_1_19_4 extends SetupImpl {
    Impl_1_19_4() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_19_4_R3.GiveImpl.INSTANCE);
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

class Impl_1_8_8 extends SetupImpl {
    Impl_1_8_8() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_8_R3.GiveImpl.INSTANCE);
    }
}

class Impl_Glowstone extends SetupImpl {
    Impl_Glowstone() {
        super(com.janboerman.invsee.spigot.addon.give.glowstone.GiveImpl.INSTANCE);
    }
}