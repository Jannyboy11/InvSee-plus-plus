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

class Impl_1_20_1 extends SetupImpl {
    Impl_1_20_1(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_20_1_R1.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_20_1_R1.KnownPlayersProvider(plugin, scheduler));
    }
}

//

class SetupImpl implements Setup {

    static SupportedServerSoftware<SetupProvider> SUPPORTED = new SupportedServerSoftware<>();
    static {
        SUPPORTED.registerSupportedVersion(ServerSoftware.CRAFTBUKKIT_1_20_1, (p, l, s, c) -> new Impl_1_20_1(p, l, s, c));
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