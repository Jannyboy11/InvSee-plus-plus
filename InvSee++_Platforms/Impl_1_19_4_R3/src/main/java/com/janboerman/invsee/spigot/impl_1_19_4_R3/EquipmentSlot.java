package com.janboerman.invsee.spigot.impl_1_19_4_R3;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

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