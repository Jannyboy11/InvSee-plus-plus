package com.janboerman.invsee.glowstone;

import net.glowstone.entity.GlowHumanEntity;
import net.glowstone.inventory.GlowInventorySlot;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

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

class DelegatingSlot extends GlowInventorySlot {
    private final GlowInventorySlot delegate;

    DelegatingSlot(GlowInventorySlot delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public ItemStack getItem() {
        return delegate.getItem();
    }

    @Override
    public void setItem(ItemStack item) {
        delegate.setItem(item);
    }

}

class PersonalSlot extends DelegatingSlot {
    PersonalSlot(GlowInventorySlot delegate) {
        super(delegate);
    }
}

class BootsSlot extends DelegatingSlot {
    BootsSlot(GlowInventorySlot delegate) {
        super(delegate);
        setEquipmentSlot(EquipmentSlot.FEET);
    }
}

class LeggingsSlot extends DelegatingSlot {
    LeggingsSlot(GlowInventorySlot delegate) {
        super(delegate);
        setEquipmentSlot(EquipmentSlot.LEGS);
    }
}

class ChestplateSlot extends DelegatingSlot {
    ChestplateSlot(GlowInventorySlot delegate) {
        super(delegate);
        setEquipmentSlot(EquipmentSlot.CHEST);
    }
}

class HelmetSlot extends DelegatingSlot {
    HelmetSlot(GlowInventorySlot delegate) {
        super(delegate);
        setEquipmentSlot(EquipmentSlot.HEAD);
    }
}

class OffhandSlot extends DelegatingSlot {
    OffhandSlot(GlowInventorySlot delegate) {
        super(delegate);
        setEquipmentSlot(EquipmentSlot.OFF_HAND);
    }
}