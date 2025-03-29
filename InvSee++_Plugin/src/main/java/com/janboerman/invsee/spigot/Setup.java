package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.version.*;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import com.janboerman.invsee.spigot.api.Scheduler;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public interface Setup {

    public InvseePlatform platform();

    public default OfflinePlayerProvider offlinePlayerProvider() {
        return OfflinePlayerProvider.Dummy.INSTANCE;
    }

    public static Setup setup(Plugin plugin, Scheduler scheduler, NamesAndUUIDs lookup, OpenSpectatorsCache cache) {
        Server server = plugin.getServer();
        ServerSoftware serverSoftware = ServerSoftware.detect(server);

        if (serverSoftware == null)
            throw new RuntimeException(SupportedServerSoftware.getUnsupportedPlatformMessage(server));

        SetupProvider provider = SetupImpl.SUPPORTED.getImplementationProvider(serverSoftware);

        if (provider == null) {
            String supportedVersionsMessage = SetupImpl.SUPPORTED.getUnsupportedVersionMessage(serverSoftware.getPlatform(), server);
            String legacyVersionsMessage = LegacyVersions.getLegacyVersionMessage(serverSoftware.getVersion());

            if (legacyVersionsMessage != null) {
                plugin.getLogger().severe(legacyVersionsMessage);
            }

            throw new RuntimeException(supportedVersionsMessage);
        }

        return provider.provide(plugin, lookup, scheduler, cache);
    }

}

//we use separate classes per implementation, to prevent classloading of an incorrect version.
//previously, the Setup#setup(Plugin) method tried to load all implementation classes, even before any of them was needed.

class Impl_1_8_8 extends SetupImpl {
    Impl_1_8_8(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_8_R3.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_8_R3.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_12_2 extends SetupImpl {
    Impl_1_12_2(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_12_R1.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_12_R1.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_16_5 extends SetupImpl {
    Impl_1_16_5(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_16_R3.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_16_R3.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_17_1 extends SetupImpl {
    Impl_1_17_1(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_17_1_R1.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_17_1_R1.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_18_2 extends SetupImpl {
    Impl_1_18_2(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_18_2_R2.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_18_2_R2.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_19_4 extends SetupImpl {
    Impl_1_19_4(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_19_4_R3.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_19_4_R3.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_20_1 extends SetupImpl {
    Impl_1_20_1(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_20_1_R1.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_20_1_R1.KnownPlayersProvider(plugin, scheduler));
    }
}


class Impl_1_20_4 extends SetupImpl {
    Impl_1_20_4(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_20_4_R3.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_20_4_R3.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_20_6 extends SetupImpl {
    Impl_1_20_6(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_20_6_R4.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_20_6_R4.KnownPlayersProvider(plugin, scheduler));
    }
}

@Deprecated
class Impl_1_21 extends SetupImpl {
    Impl_1_21(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_21_R1.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_21_R1.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_21_1 extends SetupImpl {
    Impl_1_21_1(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_21_1_R1.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_21_1_R1.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_21_3 extends SetupImpl {
    Impl_1_21_3(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_21_3_R2.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_21_3_R2.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_21_4 extends SetupImpl {
    Impl_1_21_4(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_21_4_R3.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_21_4_R3.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_1_21_5 extends SetupImpl {
    Impl_1_21_5(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_21_5_R4.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_21_5_R4.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_Glowstone extends SetupImpl {
    Impl_Glowstone(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.glowstone.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.glowstone.KnownPlayersProvider(plugin, scheduler));
    }
}

//

class SetupImpl implements Setup {

    static SupportedServerSoftware<SetupProvider> SUPPORTED = new SupportedServerSoftware<>();
    static {
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_8_8, (p, l, s, c) -> new Impl_1_8_8(p, l, s, c));
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_12_2, (p, l, s, c) -> new Impl_1_12_2(p, l, s, c));
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_16_5, (p, l, s, c) -> new Impl_1_16_5(p, l, s, c));
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_17_1, (p, l, s, c) -> new Impl_1_17_1(p, l, s, c));
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_18_2, (p, l, s, c) -> new Impl_1_18_2(p, l, s, c));
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_19_4, (p, l, s, c) -> new Impl_1_19_4(p, l, s, c));
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_1_20_1(p, l, s, c), ServerSoftware.CRAFTBUKKIT_1_20_1, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_1));
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_1_20_4(p, l, s, c), ServerSoftware.CRAFTBUKKIT_1_20_4, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_4));
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_1_20_6(p, l, s, c), ServerSoftware.CRAFTBUKKIT_1_20_6, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_20_6));
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_1_21(p, l, s, c), new ServerSoftware(MinecraftPlatform.CRAFTBUKKIT, MinecraftVersion._1_21), new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21));
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_1_21_1(p, l, s, c), ServerSoftware.CRAFTBUKKIT_1_21_1, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_1));
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_1_21_3(p, l, s, c), ServerSoftware.CRAFTBUKKIT_1_21_3, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_3));
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_1_21_4(p, l, s, c), ServerSoftware.CRAFTBUKKIT_1_21_4, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_4));
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_1_21_5(p, l, s, c), ServerSoftware.CRAFTBUKKIT_1_21_5, new ServerSoftware(MinecraftPlatform.PAPER, MinecraftVersion._1_21_5));

        final SetupProvider glowstoneProver = (p, l, s, c) -> new Impl_Glowstone(p, l, s, c);
        final MinecraftVersion[] minecraftVersions = MinecraftVersion.values();
        for (int idx = MinecraftVersion._1_8.ordinal(); idx < MinecraftVersion._1_12_2.ordinal(); idx ++) {
            SUPPORTED.registerSupportedVersion(new ServerSoftware(MinecraftPlatform.GLOWSTONE, minecraftVersions[idx]), glowstoneProver);
        }
    }

    private final InvseePlatform platform;
    private final OfflinePlayerProvider offlinePlayerProvider;

    SetupImpl(InvseePlatform platform, OfflinePlayerProvider offlinePlayerProvider) {
        this.platform = platform;
        this.offlinePlayerProvider = offlinePlayerProvider;
    }

    @Override
    public InvseePlatform platform() {
        return platform;
    }

    @Override
    public OfflinePlayerProvider offlinePlayerProvider() {
        return offlinePlayerProvider;
    }
}

interface SetupProvider {
    public Setup provide(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache);
}