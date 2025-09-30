package com.janboerman.invsee.spigot.internal.version;

import java.util.HashMap;
import java.util.Map;

public final class LegacyVersions {

    private static final LegacyVersions INSTANCE = new LegacyVersions();

    private static final LegacyVersion
            LATEST_THAT_SUPPORTS_1_20_5 = INSTANCE.register(new MinecraftVersion[] { MinecraftVersion._1_20_5, MinecraftVersion._1_15_2 }, "0.27.0",
            "https://www.spigotmc.org/resources/invsee.82342/download?version=540041", null),
            LATEST_THAT_SUPPORTS_1_20_2 = INSTANCE.register(MinecraftVersion._1_20_2, "0.26.0",
            "https://www.spigotmc.org/resources/invsee.82342/download?version=525827", "https://github.com/Jannyboy11/InvSee-plus-plus/releases/tag/v0.26.0"),
            LATEST_THAT_SUPPORTS_1_20_3 = INSTANCE.register(new MinecraftVersion[] { MinecraftVersion._1_20_1, MinecraftVersion._1_20_3 }, "0.24.9",
                    "https://www.spigotmc.org/resources/invsee.82342/download?version=522407",
                    "https://github.com/Jannyboy11/InvSee-plus-plus/releases/tag/v0.24.9"),
            LATEST_THAT_SUPPORTS_1_20 = INSTANCE.register(MinecraftVersion._1_20, "0.22.6",
            "https://www.spigotmc.org/resources/invsee.82342/download?version=505200", "https://github.com/Jannyboy11/InvSee-plus-plus/releases/tag/v0.22.6"),
            LATEST_THAT_SUPPORTS_1_19_3 = INSTANCE.register(MinecraftVersion._1_19_3, "0.21.11",
                    "https://www.spigotmc.org/resources/invsee.82342/download?version=499634", "https://github.com/Jannyboy11/InvSee-plus-plus/releases/tag/v0.22.1"),
            LATEST_THAT_SUPPORTS_1_19_2 = INSTANCE.register(MinecraftVersion._1_19_2, "0.21.11",
                    "https://www.spigotmc.org/resources/invsee.82342/download?version=499634", "https://github.com/Jannyboy11/InvSee-plus-plus/releases/tag/v0.22.1"),
            LATEST_THAT_SUPPORTS_1_19_1 = INSTANCE.register(MinecraftVersion._1_19_1, "0.14.0",
                    "https://www.spigotmc.org/resources/invsee.82342/download?version=479534", "https://github.com/Jannyboy11/InvSee-plus-plus/releases/tag/v0.14.0"),
            LATEST_THAT_SUPPORTS_1_19 = INSTANCE.register(MinecraftVersion._1_19, "0.15.2",
                    "https://www.spigotmc.org/resources/invsee.82342/download?version=480562", "https://github.com/Jannyboy11/InvSee-plus-plus/releases/tag/v0.15.2"),
            LATEST_THAT_SUPPORTS_1_18_1 = INSTANCE.register(MinecraftVersion._1_18_1, "0.12.2",
                    "https://www.spigotmc.org/resources/invsee.82342/download?version=461471", "https://github.com/Jannyboy11/InvSee-plus-plus/releases/tag/v0.12.2"),
            LATEST_THAT_SUPPORTS_1_18 = INSTANCE.register(MinecraftVersion._1_18, "0.11.10", "https://www.spigotmc.org/resources/invsee.82342/download?version=455217", null),
            LATEST_THAT_SUPPORTS_1_17 = INSTANCE.register(MinecraftVersion._1_17, "0.11.4", "https://www.spigotmc.org/resources/invsee.82342/download?version=435036", null),
            LATEST_THAT_SUPPORTS_1_16_3 = INSTANCE.register(MinecraftVersion._1_16_3, "0.7", "https://www.spigotmc.org/resources/invsee.82342/download?version=365265", null),
            LATEST_THAT_SUPPORTS_1_15_2 = LATEST_THAT_SUPPORTS_1_20_5,
            LATEST_THAT_SUPPORTS_1_21_3 = INSTANCE.register(new MinecraftVersion[] { MinecraftVersion._1_21_3, MinecraftVersion._1_21_6 }, "0.30.10",
                    "https://www.spigotmc.org/resources/invsee.82342/download?version=609062",
                    "https://github.com/Jannyboy11/InvSee-plus-plus/releases/tag/v0.30.10"),
            LATEST_THAT_SUPPORTS_1_21_6 = LATEST_THAT_SUPPORTS_1_21_3
            ;

    private final Map<MinecraftVersion, LegacyVersion> legacyInvSeePlusPlusVersions = new HashMap<>();

    private LegacyVersions() {
    }

    private LegacyVersion register(MinecraftVersion[] minecraft, String invseePlusPlus, String spigot, String github) {
        LegacyVersion legacyVersion = register(minecraft[0], invseePlusPlus, spigot, github);
        for (int i = 1; i < minecraft.length; i += 1) {
            legacyInvSeePlusPlusVersions.put(minecraft[i], legacyVersion);
        }
        return legacyVersion;
    }

    private LegacyVersion register(MinecraftVersion minecraft, String invseePlusPlus, String spigot, String github) {
        LegacyVersion legacyVersion = new LegacyVersion(minecraft, invseePlusPlus, spigot, github);
        register(legacyVersion);
        return legacyVersion;
    }

    private void register(LegacyVersion legacyVersion) {
        legacyInvSeePlusPlusVersions.put(legacyVersion.minecraftVersion, legacyVersion);
    }

    private LegacyVersion get(MinecraftVersion version) {
        return legacyInvSeePlusPlusVersions.get(version);
    }

    public static String getLegacyVersionMessage(MinecraftVersion version) {
        LegacyVersion legacyVersion = INSTANCE.get(version);
        if (legacyVersion == null)
            return null;

        String generalPart = "The latest release of InvSee++ that supported Minecraft " + version + " is InvSee++ v" + legacyVersion.invseePlusPlusVersion + ".\n";
        String spigotmcPart = "You can download this release from SpigotMC: " + legacyVersion.spigotmcDownloadUrl + "\n";
        String githubPart = legacyVersion.githubUrl == null ? "" : "It is also available on GitHub: " + legacyVersion.githubUrl + "\n";

        return generalPart + spigotmcPart + githubPart;
    }

    public static class LegacyVersion {
        private final MinecraftVersion minecraftVersion;
        private final String invseePlusPlusVersion;
        private final String spigotmcDownloadUrl;
        private final String githubUrl;

        private LegacyVersion(MinecraftVersion minecraft, String invseePlusPlus, String spigotUrl, String githubUrl) {
            this.minecraftVersion = minecraft;
            this.invseePlusPlusVersion = invseePlusPlus;
            this.spigotmcDownloadUrl = spigotUrl;
            this.githubUrl = githubUrl;
        }
    }
}
