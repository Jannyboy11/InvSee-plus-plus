package com.janboerman.invsee.spigot.impl_1_21_9_R6;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class EquipmentSlot extends Slot {

    private final ResourceLocation noItemIcon;

    EquipmentSlot(MainNmsInventory inventory, int index, int magicX, int magicY, ResourceLocation noItemIcon) {
        super(inventory, index, magicX, magicY);
        this.noItemIcon = noItemIcon;
    }

    @Nullable
    @Override
    public ResourceLocation getNoItemIcon() {
        return noItemIcon;
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

//TODO should this be an EquipmentSlot?
class BodySlot extends Slot {
    BodySlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}

//TODO should this be an EquipmentSlot?
class SaddleSlot extends Slot {
    SaddleSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}

class CursorSlot extends Slot {
    public CursorSlot(Container inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}


class PersonalSlot extends Slot {

    private final ItemStack placeholder;

    PersonalSlot(ItemStack placeholder, MainNmsInventory inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);
        this.placeholder = placeholder;
    }

    final boolean works() {
        MainNmsInventory inv = (MainNmsInventory) container;
        int personalSize = inv.personalContents.size();
        boolean inRange = 45 <= getContainerSlot() && getContainerSlot() < 45 + personalSize;
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
        if (!works()) return placeholder;
        return super.getItem();
    }

    public boolean isActive() {
        return works();
    }

}


class InaccessibleSlot extends Slot {

    private final ItemStack placeholder;

    InaccessibleSlot(ItemStack placeholder, Container inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);
        this.placeholder = placeholder;
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
        setChanged();
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
        return placeholder;
    }

    @Override
    public boolean isActive() {
        return false;
    }

}