package com.janboerman.invsee.spigot.internal.resolve;

import java.util.ArrayList;
import java.util.List;

public enum ResolveStrategyType {

    /** Get the UUID from an online player. */
    ONLINE_PLAYER("online player"),
    /** Get the UUID from InvSee++'s offline player cache. */
    LOGGED_OUT_PLAYERS_CACHE("logged out players cache"),
    /** Get the UUID from Paper's offline player cache. */
    PAPER_OFFLINE_PLAYER_CACHE("paper offline player cache"),
    /** Obtain the UUID via the permission plugin (LuckPerms, UltraPermissions, GroupManager, BungeePerms). */
    PERMISSION_PLUGIN("permission plugin"),
    /** Query the UUID from the proxy server (BungeeCord, Velocity). */
    PROXY("proxy"),
    /** Make a call to Mojang's REST api to obtain the UUID. */
    MOJANG_REST_API_CALL("mojang rest api call"),
    /** Search in the player data save files to find the UUID.. */
    PLAYER_DATA_SAVE_FILES("player data save files"),
    /** Spoof UUID based on username for servers in offline mode. */
    SPOOF("spoof"),
    /** Make a call to <a href="https://github.com/Electroid/mojang-api">Electroid's Mojang API</a> to obtain the UUID. */
    ELECTROID_REST_API_CALL("electroid mojang api call");

    private final String humanReadableName;

    private ResolveStrategyType(String configValue) {
        this.humanReadableName = configValue;
    }

    public ResolveStrategyType fromString(String configValue) {
        for (ResolveStrategyType strategy : values()) {
            if (strategy.humanReadableName.equals(configValue)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unrecognized resolve strategy: " + configValue);
    }

    public static List<ResolveStrategyType> defaultStrategies(boolean onlineMode) {
        List<ResolveStrategyType> result = new ArrayList<>();
        result.add(ONLINE_PLAYER);
        result.add(LOGGED_OUT_PLAYERS_CACHE);
        result.add(PAPER_OFFLINE_PLAYER_CACHE);
        result.add(PERMISSION_PLUGIN);
        result.add(PROXY);
        if (onlineMode) {
            result.add(MOJANG_REST_API_CALL);
        } else {
            result.add(SPOOF);
        }
        result.add(PLAYER_DATA_SAVE_FILES);
        return result;
    }
}
