package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.spigot.addon.give.common.GiveApi;
import com.janboerman.invsee.spigot.internal.version.*;
import org.bukkit.Server;

interface Setup {

    GiveApi getGiveApi();

    static Setup setup(GivePlugin plugin) {
        Server server = plugin.getServer();
        ServerSoftware serverSoftware = ServerSoftware.detect(server);

        if (serverSoftware == null)
            throw new RuntimeException(SupportedServerSoftware.getUnsupportedPlatformMessage(server));

        SetupProvider provider = SetupImpl.SUPPORTED.getImplementationProvider(serverSoftware);

        if (provider == null)
            throw new RuntimeException(SetupImpl.SUPPORTED.getUnsupportedVersionMessage(serverSoftware.getPlatform(), server));

        return provider.provide();
    }
}

interface SetupProvider {
    public Setup provide();
}

class SetupImpl implements Setup {

    static SupportedServerSoftware<SetupProvider> SUPPORTED = new SupportedServerSoftware<>();
    static {
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_8_8, () -> new Impl_1_8_8());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_12_2, () -> new Impl_1_12_2());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_16_5, () -> new Impl_1_16_5());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_17_1, () -> new Impl_1_17_1());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_18_2, () -> new Impl_1_18_2());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_19_4, () -> new Impl_1_19_4());
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_20_1(), ServerSoftware.CRAFTBUKKIT_1_20_1, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_1));
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_20_4(), ServerSoftware.CRAFTBUKKIT_1_20_4, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_4));
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_20_6(), ServerSoftware.CRAFTBUKKIT_1_20_6, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_6));
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_21(), new ServerSoftware(MinecraftPlatform.CRAFTBUKKIT, MinecraftVersion._1_21), new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21));
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_21_1(), ServerSoftware.CRAFTBUKKIT_1_21_1, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_1));
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_21_4(), ServerSoftware.CRAFTBUKKIT_1_21_4, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_4));
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_21_5(), ServerSoftware.CRAFTBUKKIT_1_21_5, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_5));
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_21_7(), ServerSoftware.CRAFTBUKKIT_1_21_7, ServerSoftware.CRAFTBUKKIT_1_21_8, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_7), new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_8));
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_21_9(), ServerSoftware.CRAFTBUKKIT_1_21_9, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_9));
        final SetupProvider glowstoneProver = () -> new Impl_Glowstone();
        final MinecraftVersion[] minecraftVersions = MinecraftVersion.values();
        for (int idx = MinecraftVersion._1_8.ordinal(); idx < MinecraftVersion._1_12_2.ordinal(); idx++) {
            SUPPORTED.registerSupportedVersion(new ServerSoftware(MinecraftPlatform.GLOWSTONE, minecraftVersions[idx]), glowstoneProver);
        }
    }

    private final GiveApi api;

    SetupImpl(GiveApi api) {
        this.api = api;
    }

    @Override
    public GiveApi getGiveApi() {
        return api;
    }
}

class Impl_1_21_9 extends SetupImpl {
    Impl_1_21_9() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_21_9_R6.GiveImpl.INSTANCE);
    }
}

class Impl_1_21_7 extends SetupImpl {
    Impl_1_21_7() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_21_7_R5.GiveImpl.INSTANCE);
    }
}

class Impl_1_21_5 extends SetupImpl {
    Impl_1_21_5() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_21_5_R4.GiveImpl.INSTANCE);
    }
}

class Impl_1_21_4 extends SetupImpl {
    Impl_1_21_4() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_21_4_R3.GiveImpl.INSTANCE);
    }
}

class Impl_1_21_1 extends SetupImpl {
    Impl_1_21_1() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_21_1_R1.GiveImpl.INSTANCE);
    }
}

@Deprecated
class Impl_1_21 extends SetupImpl {
    Impl_1_21() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_21_R1.GiveImpl.INSTANCE);
    }
}

class Impl_1_20_6 extends SetupImpl {
    Impl_1_20_6() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_20_6_R4.GiveImpl.INSTANCE);
    }
}

class Impl_1_20_4 extends SetupImpl {
    Impl_1_20_4() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_20_4_R3.GiveImpl.INSTANCE);
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
