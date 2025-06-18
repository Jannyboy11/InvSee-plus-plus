package com.janboerman.invsee.spigot.impl_1_12_R1;

import com.janboerman.invsee.utils.Compat;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderGroup;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import static com.janboerman.invsee.spigot.internal.placeholder.Placeholders.*;
import com.janboerman.invsee.spigot.internal.placeholder.SimplePlaceholderPalette;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import static org.bukkit.Material.*;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.List;

public class Placeholders {

    private static final byte WHITE = 0;
    private static final byte ORANGE = 1;
    private static final byte MAGENTA = 2;
    private static final byte LIGHT_BLUE = 3;
    private static final byte YELLOW = 4;
    private static final byte LIME = 5;
    private static final byte PINK = 6;
    private static final byte GRAY = 7;
    private static final byte LIGHT_GRAY = 8;
    private static final byte CYAN = 9;
    private static final byte PURPLE = 10;
    private static final byte BLUE = 11;
    private static final byte BROWN = 12;
    private static final byte GREEN = 13;
    private static final byte RED = 14;
    private static final byte BLACK = 15;

    // INK_SAC = 0;
    // ROSE_RED = 1;
    // CACTUS_GREEN = 2;
    // COCOA_BEANS = 3;
    private static final byte LAPIS_LAZULI = 4;
    // PURPLE_DYE = 5;
    // CYAN_DYE = 6;
    // LIGHT_GREY_DYE = 7;
    // GREY_DYE = 8;
    // PINK_DYE = 9;
    // LIME_DYE = 10;
    // YELLOW_DYE = 11;
    // LIGHT_BLUE_DYE = 12;
    // MAGENTDA_DYE = 13;
    // ORANGE_DYE = 14;
    // BONE_MEAL = 15;

    private Placeholders() {
    }

    private static final ItemStack GLASS_INACCESSIBLE = makeStack(STAINED_GLASS_PANE, BLACK, name(INACCESSIBLE));
    private static final ItemStack GLASS_ARMOUR_HELMET = makeStack(STAINED_GLASS_PANE, LIGHT_BLUE, name(HELMET));
    private static final ItemStack GLASS_ARMOUR_CHESTPLATE = makeStack(STAINED_GLASS_PANE, LIGHT_BLUE, name(CHESTPLATE));
    private static final ItemStack GLASS_ARMOUR_LEGGINGS = makeStack(STAINED_GLASS_PANE, LIGHT_BLUE, name(LEGGINGS));
    private static final ItemStack GLASS_ARMOUR_BOOTS = makeStack(STAINED_GLASS_PANE, LIGHT_BLUE, name(BOOTS));
    private static final ItemStack GLASS_OFFHAND = makeStack(STAINED_GLASS_PANE, YELLOW, name(OFFHAND));
    private static final ItemStack GLASS_CURSOR = makeStack(STAINED_GLASS_PANE, WHITE, name(CURSOR));
    private static final ItemStack GLASS_CRAFTING = makeStack(STAINED_GLASS_PANE, ORANGE, name(CRAFTING));
    private static final ItemStack GLASS_ANVIL = makeStack(STAINED_GLASS_PANE, GRAY, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.ANVIL));
    private static final ItemStack GLASS_MERCHANT = makeStack(STAINED_GLASS_PANE, LIME, name(MERCHANT));
    private static final ItemStack GLASS_ENCHANTING_ITEM = makeStack(STAINED_GLASS_PANE, BLUE, name(ENCHANTING_ITEM));
    private static final ItemStack GLASS_ENCHANTING_FUEL = makeStack(STAINED_GLASS_PANE, BLUE, name(ENCHANTING_FUEL));
    private static final ItemStack GLASS_GENERIC = makeStack(STAINED_GLASS_PANE, CYAN, name(GENERIC));

    private static final ItemStack ICON_INACCESSIBLE = makeStack(BARRIER, name(INACCESSIBLE));
    private static final ItemStack ICON_ARMOUR_HELMET = makeStack(CHAINMAIL_HELMET, and(name(HELMET), HIDE_ATTRIBUTES));
    private static final ItemStack ICON_ARMOUR_CHESTPLATE = makeStack(CHAINMAIL_CHESTPLATE, and(name(CHESTPLATE), HIDE_ATTRIBUTES));
    private static final ItemStack ICON_ARMOUR_LEGGINGS = makeStack(CHAINMAIL_LEGGINGS, and(name(LEGGINGS), HIDE_ATTRIBUTES));
    private static final ItemStack ICON_ARMOUR_BOOTS = makeStack(CHAINMAIL_BOOTS, and(name(BOOTS), HIDE_ATTRIBUTES));
    private static final ItemStack ICON_OFFHAND = makeStack(SHIELD, (BlockStateMeta meta) -> {
        meta.setDisplayName(OFFHAND);
        Banner banner = (Banner) meta.getBlockState();
        banner.setBaseColor(DyeColor.WHITE);
        banner.setPatterns(Compat.listOf(new Pattern(DyeColor.RED, PatternType.STRIPE_TOP), new Pattern(DyeColor.BLUE, PatternType.STRIPE_BOTTOM)));
        meta.setBlockState(banner);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    });
    private static final ItemStack ICON_CURSOR = makeStack(STRUCTURE_VOID, name(CURSOR));
    private static final ItemStack ICON_CRAFTING = makeStack(WORKBENCH, name(CRAFTING));
    private static final ItemStack ICON_ANVIL = makeStack(Material.ANVIL, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.ANVIL));
    private static final ItemStack ICON_MERCHANT = makeStack(EMERALD, name(MERCHANT));
    private static final ItemStack ICON_ENCHANTING_ITEM = makeStack(ENCHANTMENT_TABLE, name(ENCHANTING_ITEM));
    private static final ItemStack ICON_ENCHANTING_FUEL = makeStack(INK_SACK, LAPIS_LAZULI, name(ENCHANTING_ITEM));
    private static final ItemStack ICON_GENERIC = makeStack(CHEST, name(GENERIC));

    static final PlaceholderPalette PALETTE_GLASS = new PlaceholderPalette("glass panes",
            GLASS_INACCESSIBLE,
            GLASS_ARMOUR_HELMET,
            GLASS_ARMOUR_CHESTPLATE,
            GLASS_ARMOUR_LEGGINGS,
            GLASS_ARMOUR_BOOTS,
            GLASS_OFFHAND,
            null,
            null,
            GLASS_CURSOR,
            GLASS_CRAFTING,
            GLASS_ANVIL,
            GLASS_MERCHANT,
            null,
            GLASS_ENCHANTING_ITEM,
            GLASS_ENCHANTING_FUEL,
            null,
            null,
            null,
            null,
            null,
            null,
            GLASS_GENERIC
    );
    static final PlaceholderPalette PALETTE_ICONS = new PlaceholderPalette("icons",
            ICON_INACCESSIBLE,
            ICON_ARMOUR_HELMET,
            ICON_ARMOUR_CHESTPLATE,
            ICON_ARMOUR_LEGGINGS,
            ICON_ARMOUR_BOOTS,
            ICON_OFFHAND,
            null,
            null,
            ICON_CURSOR,
            ICON_CRAFTING,
            ICON_ANVIL,
            ICON_MERCHANT,
            null,
            ICON_ENCHANTING_ITEM,
            ICON_ENCHANTING_FUEL,
            null,
            null,
            null,
            null,
            null,
            null,
            ICON_GENERIC
    );
}

class PlaceholderPalette extends SimplePlaceholderPalette {

    public PlaceholderPalette(String name, ItemStack inaccessible, ItemStack armourHelmet, ItemStack armourChestplate, ItemStack armourLeggings, ItemStack armourBoots, ItemStack offHand, ItemStack body, ItemStack saddle, ItemStack cursor, ItemStack crafting, ItemStack anvil, ItemStack merchant, ItemStack cartography, ItemStack enchantingItem, ItemStack enchantingFuel, ItemStack grindstone, ItemStack loom, ItemStack smithingBase, ItemStack smithingTemplate, ItemStack smithingAddition, ItemStack stonecutter, ItemStack generic) {
        super(name, inaccessible, armourHelmet, armourChestplate, armourLeggings, armourBoots, offHand, body, saddle, cursor, crafting, anvil, merchant, cartography, enchantingItem, enchantingFuel, grindstone, loom, smithingBase, smithingTemplate, smithingAddition, stonecutter, generic);
    }

    @Override
    public ItemStack getPersonalSlotPlaceholder(PlayerInventorySlot slot, PlaceholderGroup group) {
        assert group != null && group.isPersonal();
        assert slot != null && slot.isPersonal();

        switch (group) {
            case INACCESSIBLE: return inaccessible();
            case ARMOUR:
                switch (slot) {
                    case ARMOUR_BOOTS: return armourBoots();
                    case ARMOUR_LEGGINGS: return armourLeggings();
                    case ARMOUR_CHESTPLATE: return armourChestplate();
                    case ARMOUR_HELMET: return armourHelmet();
                }
            case OFFHAND: return offHand();
            case CURSOR: return cursor();
            case CRAFTING: return crafting();
            case ANVIL: return anvil();
            case MERCHANT: return merchant();
            case ENCHANTING:
                switch (slot) {
                    case PERSONAL_00: return enchantingItem();
                    case PERSONAL_01: return enchantingFuel();
                }
        }

        return inaccessible();
    }

}