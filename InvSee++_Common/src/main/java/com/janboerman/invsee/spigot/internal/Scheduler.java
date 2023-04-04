package com.janboerman.invsee.spigot.internal;

//TODO add implementations for regular Bukkit, and for Folia
public interface Scheduler {

    public void executeSync(Runnable task);

    public void executeAsync(Runnable task);

    //TODO might need extra methods for executing tasks on a player-specific entity-scheduler thread
    //TODO also look carefully at which scheduler to use for saving player data files!

}
