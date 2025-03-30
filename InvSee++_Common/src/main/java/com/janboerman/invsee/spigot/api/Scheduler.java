package com.janboerman.invsee.spigot.api;

import java.util.UUID;

/**
 * Scheduler abstraction for scheduling tasks.
 *
 * @see <a href="https://github.com/Jannyboy11/InvSee-plus-plus/wiki/Folia-support">Folia support</a>
 */
public interface Scheduler {

    //TODO on places where this is called, the argument provided to the 'retired' parameter
    //TODO should actually be a value: Usually we want to:
    //TODO  - load the 'offline' player data,
    //TODO  - apply the changes to the player's inventory
    //TODO  - save the player data again.
    public void executeSyncPlayer(UUID playerId, Runnable task, Runnable retired);

    public void executeSyncGlobal(Runnable task);

    public void executeSyncGlobalRepeatedly(Runnable task, long ticksInitialDelay, long ticksPeriod);

    public void executeAsync(Runnable task);

    public void executeLaterGlobal(Runnable task, long delayTicks);

    void executeLaterAsync(Runnable task, long delayTicks);
}
