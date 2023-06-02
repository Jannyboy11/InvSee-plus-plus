package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import net.glowstone.GlowServer;
import net.glowstone.util.InventoryUtil;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

//TODO: similar implementation for InvSee++_Give

public class InvseeImpl /*TODO implements InvseePlatform*/ {

    static final ItemStack EMPTY_STACK = InventoryUtil.EMPTY_STACK;

    private final Plugin plugin;
    private final OpenSpectatorsCache cache;
    private final Scheduler scheduler;

    //TODO call this from setup.
    public InvseeImpl(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        GlowServer server = (GlowServer) plugin.getServer();
        GlowstoneHacks.injectWindowClickHandler(server);

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

        //add extra event listener for DifferenceTracker since Glowstone's InventoryView implementation does not get inventory clicks passed.
        server.getPluginManager().registerEvent(InventoryCloseEvent.class, new Listener() {}, EventPriority.MONITOR, (Listener listener, Event ev) -> {
            InventoryCloseEvent event = (InventoryCloseEvent) ev;
            InventoryView view = event.getView();
            if (view instanceof MainInventoryView) {
                ((MainInventoryView) view).onClose();
            } else if (view instanceof EnderInventoryView) {
                ((EnderInventoryView) view).onClose();
            }
        }, plugin);
    }


}
