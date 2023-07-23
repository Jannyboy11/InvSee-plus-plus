package com.janboerman.invsee.spigot.impl_1_18_2_R2;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

class CursorSlot extends Slot {
    public CursorSlot(Container inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}