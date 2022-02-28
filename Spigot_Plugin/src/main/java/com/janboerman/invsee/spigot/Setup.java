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

        InvseeAPI api = null;
        OfflinePlayerProvider offlinePlayerProvider = null;

        if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_12_R1.CraftServer")) {
            api = new com.janboerman.invsee.spigot.impl_1_12_R1.InvseeImpl(plugin);
            offlinePlayerProvider = new com.janboerman.invsee.spigot.impl_1_12_R1.KnownPlayersProvider(plugin);
        } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_15_R1.CraftServer")) {
            api = new com.janboerman.invsee.spigot.impl_1_15_R1.InvseeImpl(plugin);
            offlinePlayerProvider = new com.janboerman.invsee.spigot.impl_1_15_R1.KnownPlayersProvider(plugin);
        } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_16_R3.CraftServer")) {
            api = new com.janboerman.invsee.spigot.impl_1_16_R3.InvseeImpl(plugin);
            offlinePlayerProvider = new com.janboerman.invsee.spigot.impl_1_16_R3.KnownPlayersProvider(plugin);
        } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_17_R1.CraftServer")) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_17_1:
                    api = new com.janboerman.invsee.spigot.impl_1_17_1_R1.InvseeImpl(plugin);
                    offlinePlayerProvider = new com.janboerman.invsee.spigot.impl_1_17_1_R1.KnownPlayersProvider(plugin);
                    break;
            }
        } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_18_R1.CraftServer")) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_18:
                    api = new com.janboerman.invsee.spigot.impl_1_18_R1.InvseeImpl(plugin);
                    offlinePlayerProvider = new com.janboerman.invsee.spigot.impl_1_18_R1.KnownPlayersProvider(plugin);
                    break;
                case MappingsVersion._1_18_1:
                    api = new com.janboerman.invsee.spigot.impl_1_18_1_R1.InvseeImpl(plugin);
                    offlinePlayerProvider = new com.janboerman.invsee.spigot.impl_1_18_1_R1.KnownPlayersProvider(plugin);
                    break;
            }
        } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_18_R2.CraftServer")) {
            switch (MappingsVersion.getMappingsVersion(server)) {
                case MappingsVersion._1_18_2:
                    api = new com.janboerman.invsee.spigot.impl_1_18_2_R2.InvseeImpl(plugin);
                    offlinePlayerProvider = new com.janboerman.invsee.spigot.impl_1_18_2_R2.KnownPlayersProvider(plugin);
                    break;
            }
        }

        if (api != null) {
            assert offlinePlayerProvider != null : "offlinePlayerProvider is not null, while api is.";
            return new SetupImpl(api, offlinePlayerProvider);
        }

        throw new RuntimeException("Unsupported server software. Please run on (a fork of) CraftBukkit.");
    }

}

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