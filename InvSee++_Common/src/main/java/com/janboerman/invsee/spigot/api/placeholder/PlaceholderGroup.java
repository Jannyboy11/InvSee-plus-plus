package com.janboerman.invsee.spigot.api.placeholder;

import org.bukkit.inventory.ItemStack;

public enum PlaceholderGroup {

    INACCESSIBLE,
    ARMOUR,
    OFFHAND,
    CURSOR,
    CRAFTING,
    ANVIL,
    MERCHANT,
    CARTOGRAPHY,
    ENCHANTING,
    GRINDSTONE,
    LOOM,
    SMITHING,
    STONECUTTER;

    public ItemStack getPlaceholder(PlaceholderPalette palette, int slot) {
        switch (this) {
            case INACCESSIBLE: return palette.inaccessible();
            case ARMOUR:
                switch (slot) {
                    case 0: return palette.armourBoots();
                    case 1: return palette.armourLeggings();
                    case 2: return palette.armourChestplate();
                    case 3: return palette.armourHelmet();
                }
            case OFFHAND: return palette.offHand();
            case CURSOR: return palette.cursor();
            case CRAFTING: return palette.crafting();
            case ANVIL: return palette.anvil();
            case MERCHANT: return palette.merchant();
            case CARTOGRAPHY: return palette.cartography();
            case ENCHANTING:
                switch (slot) {
                    case 0: return palette.enchantingItem();
                    case 1: return palette.enchantingFuel();
                }
            case GRINDSTONE: return palette.grindstone();
            case LOOM: return palette.loom();
            case SMITHING:
                switch (slot) {
                    case 0: return palette.smithingBase();
                    case 1: return palette.smithingTemplate();
                    case 2: return palette.smithingAddition();
                }
            case STONECUTTER: return palette.stonecutter();
        }

        return palette.inaccessible();
    }

}
