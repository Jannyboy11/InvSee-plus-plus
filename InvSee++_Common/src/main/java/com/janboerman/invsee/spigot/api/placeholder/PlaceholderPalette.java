package com.janboerman.invsee.spigot.api.placeholder;

import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.inventory.ItemStack;

public interface PlaceholderPalette {

    public static PlaceholderPalette empty() {
        return EmptyPalette.INSTANCE;
    }

    public ItemStack inaccessible();

    public ItemStack armourHelmet();
    public ItemStack armourChestplate();
    public ItemStack armourLeggings();
    public ItemStack armourBoots();

    public ItemStack offHand();
    public ItemStack body();
    public ItemStack saddle();

    public ItemStack cursor();

    public ItemStack crafting();
    public ItemStack anvil();
    public ItemStack merchant();
    public ItemStack cartography();
    public ItemStack enchantingItem();
    public ItemStack enchantingFuel();
    public ItemStack grindstone();
    public ItemStack loom();
    public ItemStack smithingBase();
    public ItemStack smithingTemplate();
    public ItemStack smithingAddition();
    public ItemStack stonecutter();

    public ItemStack generic();

    public ItemStack getPersonalSlotPlaceholder(PlayerInventorySlot slot, PlaceholderGroup placeholderGroup);

}

class EmptyPalette implements PlaceholderPalette {

    static final EmptyPalette INSTANCE = new EmptyPalette();

    private static final ItemStack EMPTY_STACK = null;

    private EmptyPalette() {
    }

    @Override
    public String toString() {
        return "empty";
    }

    @Override
    public ItemStack inaccessible() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack armourHelmet() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack armourChestplate() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack armourLeggings() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack armourBoots() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack offHand() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack body() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack saddle() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack cursor() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack crafting() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack anvil() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack merchant() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack cartography() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack enchantingItem() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack enchantingFuel() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack grindstone() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack loom() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack smithingBase() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack smithingTemplate() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack smithingAddition() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack stonecutter() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack generic() {
        return EMPTY_STACK;
    }

    @Override
    public ItemStack getPersonalSlotPlaceholder(PlayerInventorySlot slot, PlaceholderGroup placeholderGroup) {
        return EMPTY_STACK;
    }
}