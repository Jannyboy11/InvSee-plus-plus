package com.janboerman.invsee.paper.impl_1_21_11;

/**
 * Detects whether we are running on Folia (regionised multithreading).
 * <p>
 * On Folia the target player's inventory is ticked on the target's region thread, while a spectator's
 * clicks are handled on the spectator's region thread. Directly sharing the live inventory between the
 * two (as is done on regular single-threaded servers) is a data race. When {@link #ENABLED} is true we
 * spectate a detached snapshot of an online target and commit edits back on the target's EntityScheduler.
 *
 * @see MainNmsInventory#detachSnapshotForFolia
 * @see MainNmsContainer#clicked
 */
final class FoliaSupport {

    /** True when running on a Folia server. */
    static final boolean ENABLED = detect();

    private FoliaSupport() {
    }

    private static boolean detect() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
