package com.janboerman.invsee.spigot.impl_1_8_R3;

import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;

class PlayerInventoryMirror implements Mirror<PlayerInventorySlot> {

    private static final Mirror<PlayerInventorySlot> defaultMirror = Mirror.defaultPlayerInventory();

    static final PlayerInventoryMirror INSTANCE = new PlayerInventoryMirror();

    private PlayerInventoryMirror() {}

    @Override
    public Integer getIndex(PlayerInventorySlot slot) {
        if (slot == PlayerInventorySlot.OFFHAND) {
            return null;
        } else {
            return defaultMirror.getIndex(slot);
        }
    }

    @Override
    public PlayerInventorySlot getSlot(int index) {
        if (index == PlayerInventorySlot.OFFHAND.defaultIndex()) {
            return null;
        } else {
            return defaultMirror.getSlot(index);
        }
    }
}
