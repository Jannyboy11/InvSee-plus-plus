package com.janboerman.invsee.spigot.impl_1_20_4_R3;

import com.janboerman.invsee.spigot.api.placeholder.PlaceholderGroup;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
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

import static com.janboerman.invsee.spigot.internal.placeholder.Placeholders.*;

import java.util.List;

public class Placeholders {

    private Placeholders() {
    }

    private static final ItemStack GLASS_INACCESSIBLE = makeStack(BLACK_STAINED_GLASS_PANE, name(INACCESSIBLE));
    private static final ItemStack GLASS_ARMOUR_HELMET = makeStack(LIGHT_BLUE_STAINED_GLASS_PANE, name(HELMET));
    private static final ItemStack GLASS_ARMOUR_CHESTPLATE = makeStack(LIGHT_BLUE_STAINED_GLASS_PANE, name(CHESTPLATE));
    private static final ItemStack GLASS_ARMOUR_LEGGINGS = makeStack(LIGHT_BLUE_STAINED_GLASS_PANE, name(LEGGINGS));
    private static final ItemStack GLASS_ARMOUR_BOOTS = makeStack(LIGHT_BLUE_STAINED_GLASS_PANE, name(BOOTS));
    private static final ItemStack GLASS_OFFHAND = makeStack(YELLOW_STAINED_GLASS_PANE, name(OFFHAND));
    private static final ItemStack GLASS_CURSOR = makeStack(WHITE_STAINED_GLASS_PANE, name(CURSOR));
    private static final ItemStack GLASS_CRAFTING = makeStack(ORANGE_STAINED_GLASS_PANE, name(CRAFTING));
    private static final ItemStack GLASS_ANVIL = makeStack(GRAY_STAINED_GLASS_PANE, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.ANVIL));
    private static final ItemStack GLASS_MERCHANT = makeStack(LIME_STAINED_GLASS_PANE, name(MERCHANT));
    private static final ItemStack GLASS_CARTOGRAPHY = makeStack(BROWN_STAINED_GLASS_PANE, name(CARTOGRAPHY));
    private static final ItemStack GLASS_ENCHANTING_ITEM = makeStack(BLUE_STAINED_GLASS_PANE, name(ENCHANTING_ITEM));
    private static final ItemStack GLASS_ENCHANTING_FUEL = makeStack(BLUE_STAINED_GLASS_PANE, name(ENCHANTING_FUEL));
    private static final ItemStack GLASS_GRINDSTONE = makeStack(MAGENTA_STAINED_GLASS_PANE, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.GRINDSTONE));
    private static final ItemStack GLASS_LOOM = makeStack(PINK_STAINED_GLASS_PANE, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.LOOM));
    private static final ItemStack GLASS_SMITHING_BASE = makeStack(GRAY_STAINED_GLASS_PANE, name(SMITHING_BASE));
    private static final ItemStack GLASS_SMITHING_TEMPLATE = makeStack(GRAY_STAINED_GLASS_PANE, name(SMITHING_TEMPLATE));
    private static final ItemStack GLASS_SMITHING_ADDITION = makeStack(GRAY_STAINED_GLASS_PANE, name(SMITHING_ADDITION));
    private static final ItemStack GLASS_STONECUTTER = makeStack(LIGHT_GRAY_STAINED_GLASS_PANE, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.STONECUTTER));
    private static final ItemStack GLASS_GENERIC = makeStack(CYAN_STAINED_GLASS_PANE, name(GENERIC));

    private static final ItemStack ICON_INACCESSIBLE = makeStack(BARRIER, name(INACCESSIBLE));
    private static final ItemStack ICON_ARMOUR_HELMET = makeStack(CHAINMAIL_HELMET, and(name(HELMET), HIDE_ATTRIBUTES));
    private static final ItemStack ICON_ARMOUR_CHESTPLATE = makeStack(CHAINMAIL_CHESTPLATE, and(name(CHESTPLATE), HIDE_ATTRIBUTES));
    private static final ItemStack ICON_ARMOUR_LEGGINGS = makeStack(CHAINMAIL_LEGGINGS, and(name(LEGGINGS), HIDE_ATTRIBUTES));
    private static final ItemStack ICON_ARMOUR_BOOTS = makeStack(CHAINMAIL_BOOTS, and(name(BOOTS), HIDE_ATTRIBUTES));
    private static final ItemStack ICON_OFFHAND = makeStack(SHIELD, (BlockStateMeta meta) -> {
        meta.setDisplayName(OFFHAND);
        Banner banner = (Banner) meta.getBlockState();
        banner.setBaseColor(DyeColor.WHITE);
        banner.setPatterns(List.of(new Pattern(DyeColor.RED, PatternType.STRIPE_TOP), new Pattern(DyeColor.BLUE, PatternType.STRIPE_BOTTOM)));
        meta.setBlockState(banner);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
    });
    private static final ItemStack ICON_CURSOR = makeStack(STRUCTURE_VOID, name(CURSOR));
    private static final ItemStack ICON_CRAFTING = makeStack(CRAFTING_TABLE, name(CRAFTING));
    private static final ItemStack ICON_ANVIL = makeStack(Material.ANVIL, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.ANVIL));
    private static final ItemStack ICON_MERCHANT = makeStack(EMERALD, name(MERCHANT));
    private static final ItemStack ICON_CARTOGRAPHY = makeStack(CARTOGRAPHY_TABLE, name(CARTOGRAPHY));
    private static final ItemStack ICON_ENCHANTING_ITEM = makeStack(ENCHANTING_TABLE, name(ENCHANTING_ITEM));
    private static final ItemStack ICON_ENCHANTING_FUEL = makeStack(LAPIS_LAZULI, name(ENCHANTING_FUEL));
    private static final ItemStack ICON_GRINDSTONE = makeStack(Material.GRINDSTONE, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.GRINDSTONE));
    private static final ItemStack ICON_LOOM = makeStack(Material.LOOM, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.LOOM));
    private static final ItemStack ICON_SMITHING_BASE = makeStack(SMITHING_TABLE, name(SMITHING_BASE));
    private static final ItemStack ICON_SMITHING_TEMPLATE = makeStack(NETHERITE_UPGRADE_SMITHING_TEMPLATE, name(SMITHING_TEMPLATE));
    private static final ItemStack ICON_SMITHING_ADDITION = makeStack(NETHERITE_INGOT, name(SMITHING_ADDITION));
    private static final ItemStack ICON_STONECUTTER = makeStack(Material.STONECUTTER, name(com.janboerman.invsee.spigot.internal.placeholder.Placeholders.STONECUTTER));
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
            GLASS_CARTOGRAPHY,
            GLASS_ENCHANTING_ITEM,
            GLASS_ENCHANTING_FUEL,
            GLASS_GRINDSTONE,
            GLASS_LOOM,
            GLASS_SMITHING_BASE,
            GLASS_SMITHING_TEMPLATE,
            GLASS_SMITHING_ADDITION,
            GLASS_STONECUTTER,
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
            ICON_CARTOGRAPHY,
            ICON_ENCHANTING_ITEM,
            ICON_ENCHANTING_FUEL,
            ICON_GRINDSTONE,
            ICON_LOOM,
            ICON_SMITHING_BASE,
            ICON_SMITHING_TEMPLATE,
            ICON_SMITHING_ADDITION,
            ICON_STONECUTTER,
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
            case CARTOGRAPHY: return cartography();
            case ENCHANTING:
                switch (slot) {
                    case PERSONAL_00: return enchantingItem();
                    case PERSONAL_01: return enchantingFuel();
                }
            case GRINDSTONE: return grindstone();
            case LOOM: return loom();
            case SMITHING:
                switch (slot) {
                    case PERSONAL_00: return smithingTemplate();
                    case PERSONAL_01: return smithingBase();
                    case PERSONAL_02: return smithingAddition();
                }
            case STONECUTTER: return stonecutter();
        }

        return inaccessible();
    }

}