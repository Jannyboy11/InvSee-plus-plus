package com.janboerman.invsee.spigot.internal.inventory;

import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.utils.UUIDHelper;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractNmsInventory<Slot, NMS extends AbstractNmsInventory<Slot, NMS>> implements ShallowCopy<NMS> {

    //NOTE: despite the many similarities between the different NMS inventory implementations,
    //NOTE: we can only abstract out the non-nms parts!

    public final UUID targetPlayerUuid;
    public final String targetPlayerName;

    public final String title;
    public final Mirror<Slot> mirror;
    public Inventory bukkit;

    private int maxStack;
    private final List<HumanEntity> transaction = new ArrayList<>(1);
    protected InventoryHolder owner;

    protected AbstractNmsInventory(UUID targetPlayerUuid, String targetPlayerName, String title, Mirror<Slot> mirror) {
        this.targetPlayerUuid = UUIDHelper.copy(targetPlayerUuid);
        this.targetPlayerName = targetPlayerName;
        this.title = title;
        this.mirror = mirror;
        setMaxStackSize(defaultMaxStack());
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

    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    public int getMaxStackSize() {
        return maxStack;
    }

}
