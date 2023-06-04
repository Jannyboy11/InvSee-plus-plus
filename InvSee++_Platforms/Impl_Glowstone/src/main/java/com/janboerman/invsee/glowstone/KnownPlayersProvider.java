package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.api.Scheduler;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

//TODO!
public class KnownPlayersProvider implements OfflinePlayerProvider {

    private final Plugin plugin;
    private final Scheduler scheduler;

    public KnownPlayersProvider(Plugin plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    @Override
    public void getAll(Consumer<String> consumer) {
        //TODO!
    }

    @Override
    public void getWithPrefix(String prefix, Consumer<String> consumer) {
        //TODO!
    }

}
