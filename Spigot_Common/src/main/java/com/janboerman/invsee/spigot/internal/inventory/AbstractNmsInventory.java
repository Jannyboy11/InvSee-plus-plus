package com.janboerman.invsee.spigot.internal.inventory;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.utils.UUIDHelper;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractNmsInventory<Slot, Bukkit extends SpectatorInventory<Slot>, NMS extends AbstractNmsInventory<Slot, Bukkit, NMS>> implements ShallowCopy<NMS> {

    //NOTE: despite the many similarities between the different NMS inventory implementations,
    //NOTE: we can only abstract out the non-nms parts!

    public final UUID targetPlayerUuid;
    public final String targetPlayerName;

    public final CreationOptions<Slot> creationOptions;
    private Bukkit bukkit;

    protected int maxStack; //can't abstract out the getters and setters because of remapping.
    private final List<HumanEntity> transaction = new ArrayList<>(1);
    protected InventoryHolder owner;

    protected AbstractNmsInventory(UUID targetPlayerUuid, String targetPlayerName, CreationOptions<Slot> creationOptions) {
        this.targetPlayerUuid = UUIDHelper.copy(targetPlayerUuid);
        this.targetPlayerName = targetPlayerName;
        this.maxStack = defaultMaxStack();
        this.creationOptions = creationOptions;
    }

    protected abstract Bukkit createBukkit();

    public Bukkit bukkit() {
        return bukkit == null ? bukkit = createBukkit() : bukkit;
    }

    public void onOpen(HumanEntity who) {
        transaction.add(who);
    }

    public void onClose(HumanEntity who) {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    public InventoryHolder getOwner() {
        return owner;
    }

    public Location getLocation() {
        return null;
    }

}
