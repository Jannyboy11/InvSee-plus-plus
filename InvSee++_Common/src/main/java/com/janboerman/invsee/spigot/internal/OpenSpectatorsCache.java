package com.janboerman.invsee.spigot.internal;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/*
 * TODO use this class, share it to both implementations and the InvseeAPI.
 * TODO use constructor dependency-injection.
 */
public class OpenSpectatorsCache /*TODO implement some kind of Cache interface that is api-public?*/ {

    private Map<UUID, WeakReference<MainSpectatorInventory>> openInventories = Collections.synchronizedMap(new WeakHashMap<>());
    private Map<UUID, WeakReference<EnderSpectatorInventory>> openEnderChests = Collections.synchronizedMap(new WeakHashMap<>());

    public void cache(MainSpectatorInventory spectatorInventory) {
        cache(spectatorInventory, false);
    }

    public void cache(MainSpectatorInventory spectatorInventory, boolean force) {
        WeakReference<MainSpectatorInventory> ref;
        MainSpectatorInventory oldSpectatorInv; //might want to return this.
        if (force || (ref = openInventories.get(spectatorInventory.getSpectatedPlayerId())) == null || (oldSpectatorInv = ref.get()) == null) {
            openInventories.put(spectatorInventory.getSpectatedPlayerId(), new WeakReference<>(spectatorInventory));
        }
    }

    public void cache(EnderSpectatorInventory spectatorInventory) {
        cache(spectatorInventory, false);
    }

    public void cache(EnderSpectatorInventory spectatorInventory, boolean force) {
        WeakReference<EnderSpectatorInventory> ref;
        EnderSpectatorInventory oldSpectatorInv; //might want to return this.
        if (force || (ref = openEnderChests.get(spectatorInventory.getSpectatedPlayerId())) == null || (oldSpectatorInv = ref.get()) == null) {
            openEnderChests.put(spectatorInventory.getSpectatedPlayerId(), new WeakReference<>(spectatorInventory));
        }
    }

    public MainSpectatorInventory getMainSpectatorInventory(UUID targetPlayerId) {
        WeakReference<MainSpectatorInventory> ref = openInventories.get(targetPlayerId);
        if (ref == null) return null;
        return ref.get(); //can still return null
    }

    public EnderSpectatorInventory getEnderSpectatorInventory(UUID targetPlayerId) {
        WeakReference<EnderSpectatorInventory> ref = openEnderChests.get(targetPlayerId);
        if (ref == null) return null;
        return ref.get(); //can still return null
    }
}
