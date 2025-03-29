package com.janboerman.invsee.spigot.internal.version;

import org.bukkit.Server;

import static com.janboerman.invsee.spigot.internal.version.MinecraftPlatform.*;
import static com.janboerman.invsee.spigot.internal.version.MinecraftVersion.*;

import java.util.Objects;

public class ServerSoftware {

    //only list supported server versions here:
    public static final ServerSoftware
            CRAFTBUKKIT_1_8_8 = new ServerSoftware(CRAFTBUKKIT, _1_8_8),
            CRAFTBUKKIT_1_12_2 = new ServerSoftware(CRAFTBUKKIT, _1_12_2),
            CRAFTBUKKIT_1_16_5 = new ServerSoftware(CRAFTBUKKIT, _1_16_5),
            CRAFTBUKKIT_1_17_1 = new ServerSoftware(CRAFTBUKKIT, _1_17_1),
            CRAFTBUKKIT_1_18_2 = new ServerSoftware(CRAFTBUKKIT, _1_18_2),
            CRAFTBUKKIT_1_19_4 = new ServerSoftware(CRAFTBUKKIT, _1_19_4),
            CRAFTBUKKIT_1_20_1 = new ServerSoftware(CRAFTBUKKIT, _1_20_1),
            CRAFTBUKKIT_1_20_4 = new ServerSoftware(CRAFTBUKKIT, _1_20_4),
            CRAFTBUKKIT_1_20_6 = new ServerSoftware(CRAFTBUKKIT, _1_20_6),
            CRAFTBUKKIT_1_21_1 = new ServerSoftware(CRAFTBUKKIT, _1_21_1),
            CRAFTBUKKIT_1_21_3 = new ServerSoftware(CRAFTBUKKIT, _1_21_3),
            CRAFTBUKKIT_1_21_4 = new ServerSoftware(CRAFTBUKKIT, _1_21_4),
            CRAFTBUKKIT_1_21_5 = new ServerSoftware(CRAFTBUKKIT, _1_21_5),
            GLOWSTONE_1_8_8 = new ServerSoftware(GLOWSTONE, _1_8_8),
            GLOWSTONE_1_8_9 = new ServerSoftware(GLOWSTONE, _1_8_9),
            GLOWSTONE_1_12_2 = new ServerSoftware(GLOWSTONE, _1_12_2);

    private MinecraftPlatform platform;
    private MinecraftVersion version;

    public ServerSoftware(MinecraftPlatform platform, MinecraftVersion version) {
        this.platform = platform;
        this.version = version;
    }

    public static ServerSoftware detect(final Server server) {
        final String serverClassName = server.getClass().getName();
        switch (serverClassName) {
            case "org.bukkit.craftbukkit.v1_8_R3.CraftServer":
                return CRAFTBUKKIT_1_8_8;
            case "org.bukkit.craftbukkit.v1_12_R1.CraftServer":
                return CRAFTBUKKIT_1_12_2;
            case "org.bukkit.craftbukkit.v1_15_R1.CraftServer":
                return new ServerSoftware(CRAFTBUKKIT, _1_15_2);
            case "org.bukkit.craftbukkit.v1_16_R3.CraftServer":
                return CRAFTBUKKIT_1_16_5;
            case "org.bukkit.craftbukkit.v1_17_R1.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_17: return new ServerSoftware(CRAFTBUKKIT, _1_17);
                    case CraftbukkitMappingsVersion._1_17_1: return CRAFTBUKKIT_1_17_1;
                }
            case "org.bukkit.craftbukkit.v1_18_R1.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_18: return new ServerSoftware(CRAFTBUKKIT, _1_18);
                    case CraftbukkitMappingsVersion._1_18_1: return new ServerSoftware(CRAFTBUKKIT, _1_18_1);
                }
            case "org.bukkit.craftbukkit.v1_18_R2.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_18_2: return CRAFTBUKKIT_1_18_2;
                }
            case "org.bukkit.craftbukkit.v1_19_R1.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_19: return new ServerSoftware(CRAFTBUKKIT, _1_19);
                    case CraftbukkitMappingsVersion._1_19_1: return new ServerSoftware(CRAFTBUKKIT, _1_19_1);
                    case CraftbukkitMappingsVersion._1_19_2: return new ServerSoftware(CRAFTBUKKIT, _1_19_2);
                }
            case "org.bukkit.craftbukkit.v1_19_R2.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_19_3: return new ServerSoftware(CRAFTBUKKIT, _1_19_3);
                }
            case "org.bukkit.craftbukkit.v1_19_R3.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_19_4: return CRAFTBUKKIT_1_19_4;
                }
            case "org.bukkit.craftbukkit.v1_20_R1.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_20: return new ServerSoftware(CRAFTBUKKIT, _1_20);
                    case CraftbukkitMappingsVersion._1_20_1: return CRAFTBUKKIT_1_20_1;
                }
            case "org.bukkit.craftbukkit.v1_20_R2.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_20_2: return new ServerSoftware(CRAFTBUKKIT, _1_20_2);
                }
            case "org.bukkit.craftbukkit.v1_20_R3.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_20_4:
                        //unfortunately we have to do this since CraftBukkit 1.20.3 and 1.20.4 share the same mappings version.
                        switch (server.getBukkitVersion()) {
                            case "1.20.3-R0.1-SNAPSHOT": return new ServerSoftware(CRAFTBUKKIT, _1_20_3);
                            case "1.20.4-R0.1-SNAPSHOT": return CRAFTBUKKIT_1_20_4;
                        }
                        //best-effort
                        return CRAFTBUKKIT_1_20_4;
                }
            case "org.bukkit.craftbukkit.v1_20_R4.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_20_5: return new ServerSoftware(CRAFTBUKKIT, _1_20_5);
                    case CraftbukkitMappingsVersion._1_20_6: return CRAFTBUKKIT_1_20_6;
                }
            case "org.bukkit.craftbukkit.v1_21_R1.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_21: return new ServerSoftware(CRAFTBUKKIT, _1_21);
                    case CraftbukkitMappingsVersion._1_21_1: return CRAFTBUKKIT_1_21_1;
                }
            case "org.bukkit.craftbukkit.v1_21_R2.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_21_3: return CRAFTBUKKIT_1_21_3;
                }
            case "org.bukkit.craftbukkit.v1_21_R3.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_21_4: return CRAFTBUKKIT_1_21_4;
                }
            case "org.bukkit.craftbukkit.v1_21_R4.CraftServer":
                switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                    case CraftbukkitMappingsVersion._1_21_5: return CRAFTBUKKIT_1_21_5;
                }
            case "org.bukkit.craftbukkit.CraftServer":
                // CraftBukkit 1.20.6 and up or Paper 1.20.4 and up:
                try {
                    // Call Server#getMinecraftVersion() to find out the version (this method was added by Paper).
                    return new ServerSoftware(PAPER, MinecraftVersion.fromString(server.getMinecraftVersion()));
                } catch (NoSuchMethodError nsme) {
                    // Apparently we are not running on Paper (thanks CraftBukkit...)
                    //TODO does this code-path ackshually trigger on re-obfuscated craftbukkit?
                    switch (CraftbukkitMappingsVersion.getMappingsVersion(server)) {
                        case CraftbukkitMappingsVersion._1_20_6: return CRAFTBUKKIT_1_20_6;
                        case CraftbukkitMappingsVersion._1_21_1: return CRAFTBUKKIT_1_21_1;
                        case CraftbukkitMappingsVersion._1_21_3: return CRAFTBUKKIT_1_21_3;
                        case CraftbukkitMappingsVersion._1_21_4: return CRAFTBUKKIT_1_21_4;
                        case CraftbukkitMappingsVersion._1_21_5: return CRAFTBUKKIT_1_21_5;
                    }
                }
            case "net.glowstone.GlowServer":
                final String glowstoneGameVersion = GlowstoneGameVersion.getGameVersion();
                switch (glowstoneGameVersion) {
                    case GlowstoneGameVersion._1_8_8: return GLOWSTONE_1_8_8;
                    case GlowstoneGameVersion._1_8_9: return GLOWSTONE_1_8_9;
                    case GlowstoneGameVersion._1_12_2: return GLOWSTONE_1_12_2;
                    default: return new ServerSoftware(GLOWSTONE, MinecraftVersion.fromString(glowstoneGameVersion));
                }
        }

        if (serverClassName.matches("org\\.bukkit\\.craftbukkit\\.v((.?)*)\\.CraftServer")) {
            return new ServerSoftware(CRAFTBUKKIT, null);
        }

        return null;
    }

    @Override
    public String toString() {
        return platform + " version " + version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ServerSoftware)) return false;

        ServerSoftware that = (ServerSoftware) o;
        return this.platform == that.platform && this.version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, version);
    }

    public MinecraftPlatform getPlatform() {
        return platform;
    }

    public MinecraftVersion getVersion() {
        return version;
    }

}
