package com.janboerman.invsee.spigot.internal.placeholder;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import static org.bukkit.Material.*;

import java.util.function.Consumer;

public class Placeholders {

    private Placeholders() {}

    private static final String INACESSIBLE = "";
    private static final String HELMET = "Helmet";
    private static final String CHESTPLATE = "Chestplate";
    private static final String LEGGINGS = "Leggings";
    private static final String BOOTS = "Boots";
    private static final String OFFHAND = "Off-hand";
    private static final String CURSOR = "Cursor";
    private static final String CRAFTING = "Crafting ingredient";
    private static final String ANVIL = "Anvil input";
    private static final String CARTOGRAPHY = "Cartography input";
    private static final String ENCHANTING_ITEM = "Enchanting item";
    private static final String ENCHANTING_FUEL = "Enchanting lapis lazuli";
    private static final String GRINDSTONE = "Grindstone input";
    private static final String LOOM = "Loom input";
    private static final String SMITHING_BASE = "Smithing item";
    private static final String SMITHING_TEMPLATE = "Smithing template";
    private static final String SMITHING_ADDITION = "Smithing addition";
    private static final String STONECUTTER = "Stonecutter input";

    private static final Consumer<? super ItemMeta> HIDE_ATTRIBUTES = meta -> meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

    public static final ItemStack GLASS_INACESSIBLE = makeStack(BLACK_STAINED_GLASS_PANE, name(INACESSIBLE));
    public static final ItemStack GLASS_ARMOUR_HELMET = makeStack(LIGHT_BLUE_STAINED_GLASS_PANE, name(HELMET));
    public static final ItemStack GLASS_ARMOUR_CHESTPLATE = makeStack(LIGHT_BLUE_STAINED_GLASS_PANE, name(CHESTPLATE));
    public static final ItemStack GLASS_ARMOUR_LEGGINGS = makeStack(LIGHT_BLUE_STAINED_GLASS_PANE, name(LEGGINGS));
    public static final ItemStack GLASS_ARMOUR_BOOTS = makeStack(LIGHT_BLUE_STAINED_GLASS_PANE, name(BOOTS));
    public static final ItemStack GLASS_OFFHAND = makeStack(WHITE_STAINED_GLASS_PANE, name(OFFHAND));
    public static final ItemStack GLASS_CURSOR = makeStack(WHITE_STAINED_GLASS_PANE, name(CURSOR));
    public static final ItemStack GLASS_CRAFTING = makeStack(ORANGE_STAINED_GLASS_PANE, name(CRAFTING));
    public static final ItemStack GLASS_ANVIL = makeStack(GRAY_STAINED_GLASS_PANE, name(ANVIL));
    public static final ItemStack GLASS_CARTOGRAPHY = makeStack(BROWN_STAINED_GLASS_PANE, name(CARTOGRAPHY));
    public static final ItemStack GLASS_ENCHANTING_ITEM = makeStack(BLUE_STAINED_GLASS_PANE, name(ENCHANTING_ITEM));
    public static final ItemStack GLASS_ENCHANTING_FUEL = makeStack(BLUE_STAINED_GLASS_PANE, name(ENCHANTING_FUEL));
    public static final ItemStack GLASS_GRINDSTONE = makeStack(MAGENTA_STAINED_GLASS_PANE, name(GRINDSTONE));
    public static final ItemStack GLASS_LOOM = makeStack(PINK_STAINED_GLASS_PANE, name(LOOM));
    public static final ItemStack GLASS_SMITHING_BASE = makeStack(GRAY_STAINED_GLASS_PANE, name(SMITHING_BASE));
    public static final ItemStack GLASS_SMITHING_TEMPLATE = makeStack(GRAY_STAINED_GLASS_PANE, name(SMITHING_TEMPLATE));
    public static final ItemStack GLASS_SMITHING_ADDITION = makeStack(GRAY_STAINED_GLASS_PANE, name(SMITHING_ADDITION));
    public static final ItemStack GLASS_STONECUTTER = makeStack(LIGHT_GRAY_STAINED_GLASS_PANE, name(STONECUTTER));

    public static final ItemStack REPR_INACESSIBLE = makeStack(BARRIER, name(INACESSIBLE));
    public static final ItemStack REPR_ARMOUR_HELMET = makeStack(CHAINMAIL_HELMET, and(name(HELMET), HIDE_ATTRIBUTES));
    public static final ItemStack REPR_ARMOUR_CHESTPLATE = makeStack(CHAINMAIL_CHESTPLATE, and(name(CHESTPLATE), HIDE_ATTRIBUTES));
    public static final ItemStack REPR_ARMOUR_LEGGINGS = makeStack(CHAINMAIL_LEGGINGS, and(name(LEGGINGS), HIDE_ATTRIBUTES));
    public static final ItemStack REPR_ARMOUR_BOOTS = makeStack(CHAINMAIL_BOOTS, and(name(BOOTS), HIDE_ATTRIBUTES));
    public static final ItemStack REPR_OFFHAND = makeStack(SHIELD, (BlockStateMeta meta) -> {
        meta.setDisplayName(OFFHAND);
        Banner banner = (Banner) meta.getBlockState();
        banner.setBaseColor(DyeColor.WHITE);
        banner.setPattern(0, new Pattern(DyeColor.RED, PatternType.STRIPE_TOP));
        banner.setPattern(1, new Pattern(DyeColor.BLUE, PatternType.STRIPE_BOTTOM));
        meta.setBlockState(banner);
    });
    public static final ItemStack REPR_CURSOR = makeStack(STRUCTURE_VOID, name(CURSOR));
    public static final ItemStack REPR_CRAFITNG = makeStack(CRAFTING_TABLE, name(CRAFTING));
    public static final ItemStack REPR_ANVIL = makeStack(Material.ANVIL, name(ANVIL));
    public static final ItemStack REPR_CARTOGRAPHY = makeStack(CARTOGRAPHY_TABLE, name(CARTOGRAPHY));
    public static final ItemStack REPR_ENCHANTING_ITEM = makeStack(ENCHANTING_TABLE, name(ENCHANTING_ITEM));
    public static final ItemStack REPR_ENCHANTING_FUEL = makeStack(LAPIS_LAZULI, name(ENCHANTING_FUEL));
    public static final ItemStack REPR_GRINDSTONE = makeStack(Material.GRINDSTONE, name(GRINDSTONE));
    public static final ItemStack REPR_LOOM = makeStack(Material.LOOM, name(LOOM));
    public static final ItemStack REPR_SMITHING_BASE = makeStack(SMITHING_TABLE, name(SMITHING_BASE));
    public static final ItemStack REPR_SMITHING_TEMPLATE = makeStack(NETHERITE_UPGRADE_SMITHING_TEMPLATE, name(SMITHING_TEMPLATE));
    public static final ItemStack REPR_SMITHING_ADDITION = makeStack(EMERALD, name(SMITHING_ADDITION));
    public static final ItemStack REPR_STONECUTTER = makeStack(Material.STONECUTTER, name(STONECUTTER));

    private static <IM extends ItemMeta> ItemStack makeStack(Material material, Consumer<? super IM> itemMetaModifier) {
        ItemStack stack = new ItemStack(material);
        IM meta = (IM) stack.getItemMeta();
        itemMetaModifier.accept(meta);
        stack.setItemMeta(meta);
        return stack;
    }

    private static <IM extends ItemMeta> Consumer<IM> name(String name) {
        return meta -> meta.setDisplayName(name);
    }

    private static <IM extends ItemMeta> Consumer<IM> and(Consumer<? super IM>... modifiers) {
        return meta -> { for (Consumer<? super IM> modifier : modifiers) modifier.accept(meta); };
    }
}
