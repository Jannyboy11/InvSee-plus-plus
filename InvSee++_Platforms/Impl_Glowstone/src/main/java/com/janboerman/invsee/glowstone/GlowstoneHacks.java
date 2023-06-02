package com.janboerman.invsee.glowstone;

import net.glowstone.inventory.GlowInventory;
import net.glowstone.inventory.GlowInventorySlot;

import java.lang.reflect.Field;
import java.util.List;

final class GlowstoneHacks {

    static List<GlowInventorySlot> getSlots(GlowInventory inventory) {
        try {
            Field field = GlowInventory.class.getDeclaredField("slots");
            field.setAccessible(true);
            Object slots = field.get(inventory);
            return (List<GlowInventorySlot>) slots;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get GlowInventory slots reflectively.");
        }
    }

    static void setSlots(GlowInventory inventory, List<GlowInventorySlot> slots) {
        try {
            Field field = GlowInventory.class.getDeclaredField("slots");
            field.setAccessible(true);
            field.set(inventory, slots);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set GlowInventory slots reflectively.");
        }
    }

}
