package com.janboerman.invsee.glowstone;

import net.glowstone.entity.GlowHumanEntity;
import net.glowstone.inventory.GlowInventorySlot;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

final class InaccessibleSlot extends GlowInventorySlot {

    static final GlowInventorySlot INSTANCE = new InaccessibleSlot();

    private InaccessibleSlot() {
        super(SlotType.OUTSIDE);
    }

    @Override
    public ItemStack getItem() {
        return InvseeImpl.EMPTY_STACK;
    }

    @Override
    public void setItem(ItemStack stack) {
    }

}

final class CursorSlot extends GlowInventorySlot {

    private final GlowHumanEntity target;

    CursorSlot(GlowHumanEntity target) {
        super(target.getItemOnCursor());
        this.target = target;
    }

    @Override
    public ItemStack getItem() {
        ItemStack stack = target.getItemOnCursor();
        if (stack == null) stack = InvseeImpl.EMPTY_STACK;
        return stack;
    }

    @Override
    public void setItem(ItemStack item) {
        if (item == null) item = InvseeImpl.EMPTY_STACK;
        target.setItemOnCursor(item);
    }
}