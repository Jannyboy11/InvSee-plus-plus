package com.janboerman.invsee.spigot.impl_1_20_1_R1;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

class EquipmentSlot extends Slot {

    private final ResourceLocation noItemIcon;

    EquipmentSlot(MainNmsInventory inventory, int index, int magicX, int magicY, ResourceLocation noItemIcon) {
        super(inventory, index, magicX, magicY);
        this.noItemIcon = noItemIcon;
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, noItemIcon);
    }

}

class BootsSlot extends EquipmentSlot {
    BootsSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
    }
}

class LeggingsSlot extends EquipmentSlot {
    LeggingsSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
    }
}

class ChestplateSlot extends EquipmentSlot {
    ChestplateSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
    }
}

class HelmetSlot extends EquipmentSlot {
    HelmetSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
    }
}

class OffhandSlot extends EquipmentSlot {
    OffhandSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
    }
}

class CursorSlot extends Slot {
    public CursorSlot(Container inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}


class PersonalSlot extends Slot {

    PersonalSlot(MainNmsInventory inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);
    }

    final boolean works() {
        MainNmsInventory inv = (MainNmsInventory) super.container;		                                    // Mohist compat: use super.container instead of this.container
        int personalSize = inv.personalContents.size();
        boolean inRange = 45 <= super.getContainerSlot() && super.getContainerSlot() < 45 + personalSize;   // Mohist compat: use super.getContainerSlot() instead of this.getContainerSlot()
        return inRange;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if (!works()) return false;
        return super.mayPlace(itemStack);
    }

    @Override
    public boolean hasItem() {
        if (!works()) return false;
        return super.hasItem();
    }

    @Override
    public void set(ItemStack itemStack) {
        if (!works()) this.setChanged();
        super.set(itemStack);
    }

    @Override
    public int getMaxStackSize() {
        if (!works()) return 0;
        return super.getMaxStackSize();
    }

    @Override
    public ItemStack remove(int subtractAmount) {
        if (!works()) {
            return ItemStack.EMPTY;
        } else {
            return super.remove(subtractAmount);
        }
    }

    @Override
    public boolean allowModification(Player player) {
        if (!works()) return false;
        return super.allowModification(player);
    }

    @Override
    public boolean mayPickup(Player player) {
        if (!works()) return false;
        return super.mayPickup(player);
    }

    @Override
    public ItemStack getItem() {
        if (!works()) return ItemStack.EMPTY;
        return super.getItem();
    }

    public boolean isActive() {
        return works();
    }

    // === Mohist workarounds ===
    // For now, use the same SRG names as in 1.19.4, as they seem to be stable.

    public boolean m_5857_(ItemStack itemStack) {
        return mayPlace(itemStack);
    }

    public boolean m_6657_() {
        return hasItem();
    }

    public void m_219996_(ItemStack itemStack) {
        set(itemStack);
    }

    public int m_6641_() {
        return getMaxStackSize();
    }

    public ItemStack m_6201_(int subtractAmount) {
        return remove(subtractAmount);
    }

    public boolean m_150651_(Player player) {
        return allowModification(player);
    }

    public boolean m_8010_(Player player) {
        return mayPickup(player);
    }

    public ItemStack m_7993_() {
        return getItem();
    }

    public boolean m_6659_() {
        return isActive();
    }

}


class InaccessibleSlot extends Slot {

    InaccessibleSlot(Container inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean hasItem() {
        return false;
    }

    @Override
    public void set(ItemStack itemStack) {
        super.setChanged();		// Mohist: call super.setChanged() instead of this.setChanged().
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public ItemStack remove(int subtractAmount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean allowModification(Player player) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isActive() {
        return false;
    }


    // === Mohist workarounds ===
    // For now, use the same SRG names as in 1.19.4, as they seem to be stable.

    public boolean m_5857_(ItemStack itemStack) {
        return mayPlace(itemStack);
    }

    public boolean m_6657_() {
        return hasItem();
    }

    public void m_219996_(ItemStack itemStack) {
        set(itemStack);
    }

    public int m_6641_() {
        return getMaxStackSize();
    }

    public ItemStack m_6201_(int subtractAmount) {
        return remove(subtractAmount);
    }

    public boolean m_150651_(Player player) {
        return allowModification(player);
    }

    public boolean m_8010_(Player player) {
        return mayPickup(player);
    }

    public ItemStack m_7993_() {
        return getItem();
    }

    public boolean m_6659_() {
        return isActive();
    }

}
