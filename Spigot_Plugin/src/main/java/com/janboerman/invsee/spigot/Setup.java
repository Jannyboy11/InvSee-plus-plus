package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.internal.MappingsVersion;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public interface Setup {

    public InvseeAPI api();

    public default OfflinePlayerProvider offlinePlayerProvider() {
        return OfflinePlayerProvider.Dummy.INSTANCE;
    }

    public static Setup setup(Plugin plugin) {
        final Server server = plugin.getServer();
        final String serverClassName = server.getClass().getName();

        if ("org.bukkit.craftbukkit.v1_12_R1.CraftServer".equals(serverClassName)) {
            return new Impl_1_12_2(plugin);
        } else if ("org.bukkit.craftbukkit.v1_15_R1.CraftServer".equals(serverClassName)) {
            return new Impl_1_15_2(plugin);
        } else if ("org.bukkit.craftbukkit.v1_16_R3.CraftServer".equals(serverClassName)) {
            return new Impl_1_16_5(plugin);
        } else if ("org.bukkit.craftbukkit.v1_17_R1.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_17_1:
                    return new Impl_1_17_1(plugin);
            }
        } else if ("org.bukkit.craftbukkit.v1_18_R2.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_18_2:
                    return new Impl_1_18_2(plugin);
            }
        } else if ("org.bukkit.craftbukkit.v1_19_R1.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_19_2:
                    return new Impl_1_19_2(plugin);
            }
        } else if ("org.bukkit.craftbukkit.v1_19_R2.CraftServer".equals(serverClassName)) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_19_3:
                    return new Impl_1_19_3(plugin);
            }
        }

        if (server.getClass().getSimpleName().equals("CraftServer")) {
            throw new RuntimeException("Unsupported CraftBukkit version. Please run on one of [1.12.2, 1.15.2, 1.16.5, 1.17.1, 1.18.2, 1.19.2, 1.19.3]. Are you running the latest InvSee++?");
        } else {
            throw new RuntimeException("Unsupported server software. Please run on (a fork of) CraftBukkit.");
        }
    }

}

//we use separate classes per implementation, to prevent classloading of an incorrect version.
//previously, the Setup#setup(Plugin) method tried to load all implementation classes, even before any of them was needed.

class Impl_1_12_2 extends SetupImpl {
    Impl_1_12_2(Plugin plugin) {
        super(new com.janboerman.invsee.spigot.impl_1_12_R1.InvseeImpl(plugin), new com.janboerman.invsee.spigot.impl_1_12_R1.KnownPlayersProvider(plugin));
    }
}

class Impl_1_15_2 extends SetupImpl {
    Impl_1_15_2(Plugin plugin) {
        super(new com.janboerman.invsee.spigot.impl_1_15_R1.InvseeImpl(plugin), new com.janboerman.invsee.spigot.impl_1_15_R1.KnownPlayersProvider(plugin));
    }
}

class Impl_1_16_5 extends SetupImpl {
    Impl_1_16_5(Plugin plugin) {
        super(new com.janboerman.invsee.spigot.impl_1_16_R3.InvseeImpl(plugin), new com.janboerman.invsee.spigot.impl_1_16_R3.KnownPlayersProvider(plugin));
    }
}

class Impl_1_17_1 extends SetupImpl {
    Impl_1_17_1(Plugin plugin) {
        super(new com.janboerman.invsee.spigot.impl_1_17_1_R1.InvseeImpl(plugin), new com.janboerman.invsee.spigot.impl_1_17_1_R1.KnownPlayersProvider(plugin));
    }
}

class Impl_1_18_2 extends SetupImpl {
    Impl_1_18_2(Plugin plugin) {
        super(new com.janboerman.invsee.spigot.impl_1_18_2_R2.InvseeImpl(plugin), new com.janboerman.invsee.spigot.impl_1_18_2_R2.KnownPlayersProvider(plugin));
    }
}

class Impl_1_19_2 extends SetupImpl {
    Impl_1_19_2(Plugin plugin) {
        super(new com.janboerman.invsee.spigot.impl_1_19_2_R1.InvseeImpl(plugin), new com.janboerman.invsee.spigot.impl_1_19_2_R1.KnownPlayersProvider(plugin));
    }
}

class Impl_1_19_3 extends SetupImpl {
    Impl_1_19_3(Plugin plugin) {
        super(new com.janboerman.invsee.spigot.impl_1_19_3_R2.InvseeImpl(plugin), new com.janboerman.invsee.spigot.impl_1_19_3_R2.KnownPlayersProvider(plugin));
    }
}

//

class SetupImpl implements Setup {

    private final InvseeAPI api;
    private final OfflinePlayerProvider offlinePlayerProvider;

    SetupImpl(InvseeAPI api, OfflinePlayerProvider offlinePlayerProvider) {
        this.api = api;
        this.offlinePlayerProvider = offlinePlayerProvider;
    }

    @Override
    public InvseeAPI api() {
        return api;
    }

    @Override
    public OfflinePlayerProvider offlinePlayerProvider() {
        return offlinePlayerProvider;
    }
}