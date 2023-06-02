package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import net.glowstone.util.InventoryUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class InvseeImpl {

    static final ItemStack EMPTY_STACK = InventoryUtil.EMPTY_STACK;

    private final Plugin plugin;
    private final OpenSpectatorsCache cache;
    private final Scheduler scheduler;

    public InvseeImpl(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.scheduler = scheduler;

        if (lookup.onlineMode(plugin.getServer())) {
            //TODO add UUIDSearchSaveFilesStrategy
        } else {
            //TODO add UUIDSearchSaveFilesStrategy beforelast
            //TODO check GlowStone's UUID spoofing method
        }
        //TODO add NameSearchSaveFilesStrategy
    }


}
