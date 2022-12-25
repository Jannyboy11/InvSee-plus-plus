package com.janboerman.invsee.spigot.api.template;

import com.janboerman.invsee.spigot.internal.template.EnderChestMirror;
import com.janboerman.invsee.spigot.internal.template.PlayerInventoryMirror;

public interface Mirror<Slot> {

    public Integer getIndex(Slot slot);

    public Slot getSlot(int index);

    public static Mirror<PlayerInventorySlot> defaultPlayerInventory() {
        return PlayerInventoryMirror.DEFAULT;
    }

    public static Mirror<EnderChestSlot> defaultEnderChest() {
        return EnderChestMirror.DEFAULT;
    }

    public static Mirror<PlayerInventorySlot> forInventory(String template) {
        return new PlayerInventoryMirror(template);
    }

    public static Mirror<EnderChestSlot> forEnderChest(String template) {
        return new EnderChestMirror(template);
    }
}

