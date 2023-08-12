package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.internal.inventory.ShallowCopy;
import com.janboerman.invsee.utils.UUIDHelper;
import net.glowstone.entity.GlowHumanEntity;
import net.glowstone.inventory.GlowInventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

class EnderInventory extends GlowInventory implements EnderSpectatorInventory, ShallowCopy<EnderInventory> {

    final UUID targetPlayerUuid;
    final String targetPlayerName;

    final CreationOptions<EnderChestSlot> creationOptions;

    public EnderInventory(GlowHumanEntity target, CreationOptions<EnderChestSlot> creationOptions) {
        super(null, InventoryType.CHEST, target.getEnderChest().getSize(), creationOptions.getTitle().titleFor(Target.byGameProfile(target.getUniqueId(), target.getName())));

        this.targetPlayerUuid = UUIDHelper.copy(target.getUniqueId());
        this.targetPlayerName = target.getName();
        this.creationOptions = creationOptions;
        setMaxStackSize(defaultMaxStack());

        GlowstoneHacks.setSlots(this, GlowstoneHacks.getSlots(target.getEnderChest()));
    }

    @Override
    public String getSpectatedPlayerName() {
        return targetPlayerName;
    }

    @Override
    public UUID getSpectatedPlayerId() {
        return targetPlayerUuid;
    }

    @Override
    public Mirror<EnderChestSlot> getMirror() {
        return creationOptions.getMirror();
    }

    @Override
    public CreationOptions<EnderChestSlot> getCreationOptions() {
        return creationOptions.clone();
    }

    @Override
    public int defaultMaxStack() {
        return 64;
    }

    @Override
    public void shallowCopyFrom(EnderInventory from) {
        GlowstoneHacks.setSlots(this, GlowstoneHacks.getSlots(from));
    }

    @Override
    public void setContents(EnderSpectatorInventory newContents) {
        super.setContents(newContents.getContents());
    }

    @Override
    public ItemStack[] getStorageContents() {
        return super.getContents();
    }

    @Override
    public void setStorageContents(ItemStack[] contents) {
        super.setContents(contents);
    }

    @Override
    public void forEach(Consumer<? super ItemStack> action) {
        getSlots().forEach(slot -> action.accept(slot.getItem()));
    }

    // Glowstone faulty implementations overrides

    @Override
    public boolean containsAtLeast(ItemStack stack, int amount) {
        if (amount <= 0)
            return true;
        if (stack == null)
            return false; //this is a bit weird, but this is what CraftInventory does.

        int encountered = 0;
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item != null && item.isSimilar(stack)) {
                encountered += item.getAmount();
                if (encountered >= amount)
                    return true;
            }
        }

        return false;
    }

}
