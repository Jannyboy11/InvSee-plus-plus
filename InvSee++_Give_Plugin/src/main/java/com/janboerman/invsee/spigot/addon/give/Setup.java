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
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_15_2, () -> new Impl_1_15_2());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_16_5, () -> new Impl_1_16_5());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_17_1, () -> new Impl_1_17_1());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_18_2, () -> new Impl_1_18_2());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_19_4, () -> new Impl_1_19_4());
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_20_1, () -> new Impl_1_20_1());
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_20_2(), ServerSoftware.CRAFTBUKKIT_1_20_2, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_2));
        SUPPORTED.registerSupportedVersion(() -> new Impl_1_20_3(), ServerSoftware.CRAFTBUKKIT_1_20_3, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_3));
        final SetupProvider glowstoneProver = () -> new Impl_Glowstone();
        final MinecraftVersion[] minecraftVersions = MinecraftVersion.values();
        for (int idx = MinecraftVersion._1_8.ordinal(); idx < MinecraftVersion._1_12_2.ordinal(); idx ++) {
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

class Impl_1_20_3 extends SetupImpl {
    Impl_1_20_3() {
        super(com.janboerman.invsee.spigot.addon.give.impl_1_20_3_R3.GiveImpl.INSTANCE);
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
