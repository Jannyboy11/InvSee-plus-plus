package com.janboerman.invsee.spigot.internal.version;

import org.bukkit.Server;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SupportedServerSoftware<ImplementationProvider> {

    private final Map<ServerSoftware, ImplementationProvider> supportedVersions = new HashMap<>();

    public void registerSupportedVersion(ServerSoftware software, ImplementationProvider implementationSupplier) {
        this.supportedVersions.put(software, implementationSupplier);
    }

    public ImplementationProvider getImplementationProvider(ServerSoftware software) {
        return supportedVersions.get(software);
    }

    public static String getUnsupportedPlatformMessage(Server server) {
        return server.getName() + " is not supported. Please run InvSee++ on (a fork of) one of the following server software: "
                + Arrays.stream(MinecraftPlatform.values())
                        .map(MinecraftPlatform::toString)
                        .collect(Collectors.joining(", ", "[", "]")) + ".";
    }

    public String getUnsupportedVersionMessage(MinecraftPlatform platform, Server server) {
        return platform + " version " + server.getVersion() + " is not supported by this release of InvSee++. "
                + "Please use one of the following " + platform + " versions: " + supportedVersions.keySet().stream()
                        .filter(software -> software.getPlatform() == platform)
                        .map(software -> software.getVersion().toString())
                        .collect(Collectors.joining(", ", "[", "]")) + ". "
                + "Alternatively you can try upgrading InvSee++ if any of the versions listed here is older than your server's Minecraft version. "
                + "InvSee++ is available on SpigotMC (recommended): " + "https://www.spigotmc.org/resources/invsee.82342/" + " and on GitHub: "
                + "https://github.com/Jannyboy11/InvSee-plus-plus/releases";
    }

}
