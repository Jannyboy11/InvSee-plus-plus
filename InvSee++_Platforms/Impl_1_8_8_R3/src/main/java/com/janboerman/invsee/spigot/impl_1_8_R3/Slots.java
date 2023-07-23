package com.janboerman.invsee.spigot.impl_1_8_R3;

import net.minecraft.server.v1_8_R3.IInventory;
import net.minecraft.server.v1_8_R3.Slot;

class CursorSlot extends Slot {
    public CursorSlot(IInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}

class BootsSlot extends Slot {
    BootsSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}

class LeggingsSlot extends Slot {
    LeggingsSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}

class ChestplateSlot extends Slot {
    ChestplateSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}

class HelmetSlot extends Slot {
    HelmetSlot(MainNmsInventory inventory, int index, int magicX, int magicY) {
        super(inventory, index, magicX, magicY);
    }
}