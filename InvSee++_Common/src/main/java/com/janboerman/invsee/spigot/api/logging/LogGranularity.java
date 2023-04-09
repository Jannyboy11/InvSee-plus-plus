package com.janboerman.invsee.spigot.api.logging;

/**
 * Granularity for logging.
 */
public enum LogGranularity {

    /** Log never. */
    LOG_NEVER,
    /** Log when the {@link com.janboerman.invsee.spigot.api.SpectatorInventoryView} closes. */
    LOG_ON_CLOSE,
    /** Log changes after every change to the {@link com.janboerman.invsee.spigot.api.SpectatorInventory}. */
    LOG_EVERY_CHANGE,

}
