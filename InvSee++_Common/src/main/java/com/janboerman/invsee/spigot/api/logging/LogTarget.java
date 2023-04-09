package com.janboerman.invsee.spigot.api.logging;

/**
 * Output for logging
 */
public enum LogTarget {

    /** Output to the console. */
    CONSOLE,
    /** Output to the server log file (latest.log). */
    SERVER_LOG_FILE,
    /** Output to the plugin's log file (to be found in the data folder). */
    PLUGIN_LOG_FILE,
    /** Output to the player-specific log file (to be found in the plugin's data folder). */
    SPECTATOR_LOG_FILE;

}
