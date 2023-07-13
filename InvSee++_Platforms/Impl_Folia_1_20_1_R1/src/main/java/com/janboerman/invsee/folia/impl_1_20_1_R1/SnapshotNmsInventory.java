package com.janboerman.invsee.folia.impl_1_20_1_R1;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/* A SnapshotNmsInventory. Differences are calculated against this inventory.
 * Resulting differences will be applied to the real target inventory.
 *
 * Most methods of this class will be called from a thread owned by the spectator's entity scheduler.
 * If not, it must be explicitly stated!
 */
class SnapshotNmsInventory implements Container {

    private final SimpleContainer wrapped;

    final AtomicReference<SnapshotState> state = new AtomicReference<>(SnapshotState.EMPTY);
    final Executor spectatorThread;
    final Executor targetThread;

    volatile List<ItemStack> lastCommitted;

    //also *must* be called from a thread owned by spectator's entity scheduler.
    SnapshotNmsInventory(int size, Executor spectatorThread, Executor targetThread) {
        this.wrapped = new SimpleContainer(size);
        this.spectatorThread = spectatorThread;
        this.targetThread = targetThread;
    }

    //called from thread from Target's entity scheduler.
    void fill(Container live, Executor spectatorThread) {
        if (state.compareAndExchangeAcquire(SnapshotState.EMPTY, SnapshotState.BUSY) != SnapshotState.EMPTY)
            throw new ConcurrentModificationException("Tried filling up SnapshotInventory while not EMPTY");
        if (live.getContainerSize() != getContainerSize()) //
            throw new IllegalArgumentException("Live inventory must have size " + getContainerSize());

        ConcurrentLinkedQueue<ItemStack> toBeProcessed = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < live.getContainerSize(); i++) {
            toBeProcessed.offer(live.getItem(i).copy());
        }

        lastCommitted = List.copyOf(toBeProcessed);

        spectatorThread.execute(() -> {
            int i = 0;
            while (!toBeProcessed.isEmpty()) {
                wrapped.setItem(i++, toBeProcessed.poll());
            }

            if (state.compareAndExchangeRelease(SnapshotState.BUSY, SnapshotState.READY) != SnapshotState.BUSY)
                throw new ConcurrentModificationException("Different thread completed filling the SnapshotNmsInventory??");
        });
    }


    // ==== NMS overrides ====

    //TODO important methods to customise since they are called by Slot:
    //TODO getItem, setItem, setChanged.

    @Override
    public int getContainerSize() {
        return wrapped.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return wrapped.getItem(index);
    }

    @Override
    public ItemStack removeItem(int index, int amount) {
        return wrapped.removeItem(index, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return wrapped.removeItemNoUpdate(index);
    }

    @Override
    public void setItem(int index, ItemStack itemStack) {
        wrapped.setItem(index, itemStack);
    }

    @Override
    public int getMaxStackSize() {
        return wrapped.getMaxStackSize();
    }

    @Override
    public void setChanged() {
        wrapped.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return wrapped.stillValid(player);
    }

    @Override
    public List<ItemStack> getContents() {
        return wrapped.getContents();
    }

    @Override
    public void onOpen(CraftHumanEntity craftHumanEntity) {
        wrapped.onOpen(craftHumanEntity);
    }

    @Override
    public void onClose(CraftHumanEntity craftHumanEntity) {
        wrapped.onClose(craftHumanEntity);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return wrapped.getViewers();
    }

    @Override
    public InventoryHolder getOwner() {
        return wrapped.getOwner();
    }

    @Override
    public void setMaxStackSize(int maxStackSize) {
        wrapped.setMaxStackSize(maxStackSize);
    }

    @Override
    public Location getLocation() {
        return wrapped.getLocation();
    }

    @Override
    public void clearContent() {
        wrapped.clearContent();
    }

}
