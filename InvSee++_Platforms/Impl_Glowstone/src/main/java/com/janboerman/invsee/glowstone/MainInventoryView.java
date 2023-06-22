package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.logging.LogOutput;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.List;

class MainInventoryView extends MainSpectatorInventoryView {

    private final HumanEntity spectator;
    private final MainInventory top;
    private final PlayerInventory bottom;

    private DifferenceTracker diffTracker;

    InventoryOpenEvent openEvent;

    MainInventoryView(HumanEntity spectator, MainInventory top, CreationOptions<PlayerInventorySlot> creationOptions) {
        super(creationOptions);
        this.spectator = spectator;
        this.bottom = spectator.getInventory();
        this.top = top;

        final Target target = Target.byGameProfile(top.getSpectatedPlayerId(), top.getSpectatedPlayerName());
        final LogOptions logOptions = creationOptions.getLogOptions();
        final Plugin plugin = creationOptions.getPlugin();
        if (!LogOptions.isEmpty(logOptions)) {
            diffTracker = new DifferenceTracker(
                    LogOutput.make(plugin, spectator.getUniqueId(), spectator.getName(), target, logOptions),
                    logOptions.getGranularity());
            diffTracker.onOpen();
        }
    }

    @Override
    public MainInventory getTopInventory() {
        return top;
    }

    @Override
    public PlayerInventory getBottomInventory() {
        return bottom;
    }

    @Override
    public HumanEntity getPlayer() {
        return spectator;
    }

    @Override
    public ItemStack getItem(int slot) {
        MainInventory top;
        if (0 <= slot && slot < (top = getTopInventory()).getSize()) {
            PlayerInventorySlot piSlot = getMirror().getSlot(slot);
            return piSlot == null ? null : top.getItem(piSlot.defaultIndex());
        } else {
            return super.getItem(slot);
        }
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        MainInventory top;
        if (0 <= slot && slot < (top = getTopInventory()).getSize()) {
            PlayerInventorySlot piSlot = getMirror().getSlot(slot);
            if (piSlot != null) top.setItem(piSlot.defaultIndex(), item);
        } else {
            super.setItem(slot, item);
        }
    }

    @Nullable
    @Override
    public Difference getTrackedDifference() {
        return diffTracker == null ? null : diffTracker.getDifference();
    }

    void onClick(List<ItemStack> oldItems, List<ItemStack> newItems) {
        if (diffTracker != null)
            diffTracker.onClick(oldItems, newItems);
    }

    void onClose() {
        if (diffTracker != null)
            diffTracker.onClose();
    }


}
