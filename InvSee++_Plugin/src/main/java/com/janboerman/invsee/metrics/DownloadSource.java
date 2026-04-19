package com.janboerman.invsee.metrics;

import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public enum DownloadSource {

    SPIGOTMC,
    MODRINTH,
    GITHUB,
    HANGAR,
    UNKNOWN;

    @Override
    public String toString() {
        switch (this) {
            case SPIGOTMC: return "SpigotMC";
            case MODRINTH: return "Modrinth";
            case HANGAR: return "Hangar";
            case GITHUB: return "GitHub";
            default: return "Unknown";
        }
    }

    private static DownloadSource fromString(String line) {
        if (line == null) return UNKNOWN;
        switch (line) {
            case "SpigotMC": return SPIGOTMC;
            case "Modrinth": return MODRINTH;
            case "Hangar": return HANGAR;
            case "GitHub": return GITHUB;
            default: return UNKNOWN;
        }
    }

    public static DownloadSource detect(Plugin plugin) {
        try (InputStream inputStream = plugin.getClass().getResourceAsStream("/download-source.txt")) {
            if (inputStream == null) return UNKNOWN;
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                return fromString(bufferedReader.readLine());
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read download-source.txt from jar file.", e);
            return UNKNOWN;
        }
    }

}
