package com.janboerman.invsee.spigot.internal;

import java.util.UUID;

public interface Scheduler {

    public void executeSyncPlayer(UUID playerId, Runnable task, Runnable retired);

    public void executeSyncGlobal(Runnable task);

    public void executeAsync(Runnable task);

}
