package com.janboerman.invsee.spigot.internal.placeholder;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Placeholders {

    private Placeholders() {
    }

    public static final String INACCESSIBLE = ChatColor.DARK_RED + "";
    public static final String HELMET = ChatColor.AQUA + "Helmet";
    public static final String CHESTPLATE = ChatColor.AQUA + "Chestplate";
    public static final String LEGGINGS = ChatColor.AQUA + "Leggings";
    public static final String BOOTS = ChatColor.AQUA + "Boots";
    public static final String OFFHAND = ChatColor.AQUA + "Off-hand";
    public static final String BODY = ChatColor.AQUA + "Body";
    public static final String SADDLE = ChatColor.AQUA + "Saddle";
    public static final String CURSOR = ChatColor.AQUA + "Cursor";
    public static final String CRAFTING = ChatColor.AQUA + "Crafting ingredient";
    public static final String ANVIL = ChatColor.AQUA + "Anvil input";
    public static final String MERCHANT = ChatColor.AQUA + "Merchant payment";
    public static final String CARTOGRAPHY = ChatColor.AQUA + "Cartography input";
    public static final String ENCHANTING_ITEM = ChatColor.AQUA + "Enchanting item";
    public static final String ENCHANTING_FUEL = ChatColor.AQUA + "Enchanting lapis lazuli";
    public static final String GRINDSTONE = ChatColor.AQUA + "Grindstone input";
    public static final String LOOM = ChatColor.AQUA + "Loom input";
    public static final String SMITHING_BASE = ChatColor.AQUA + "Smithing item";
    public static final String SMITHING_TEMPLATE = ChatColor.AQUA + "Smithing template";
    public static final String SMITHING_ADDITION = ChatColor.AQUA + "Smithing addition";
    public static final String STONECUTTER = ChatColor.AQUA + "Stonecutter input";
    public static final String GENERIC = ChatColor.AQUA + "";

    public static final Consumer<? super ItemMeta> HIDE_ATTRIBUTES = meta -> meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

    public static <IM extends ItemMeta> ItemStack makeStack(Material material, Consumer<? super IM> itemMetaModifier) {
        ItemStack stack = new ItemStack(material);
        modifyStack(stack, itemMetaModifier);
        return stack;
    }

    public static <IM extends ItemMeta> ItemStack makeStack(Material material, byte dataValue, Consumer<? super IM> itemMetaModifier) {
        ItemStack stack = new ItemStack(material, 1, (short) 0, dataValue);
        modifyStack(stack, itemMetaModifier);
        return stack;
    }

    public static <IM extends ItemMeta> void modifyStack(ItemStack itemStack, Consumer<? super IM> itemMetaModifier) {
        IM meta = (IM) itemStack.getItemMeta();
        itemMetaModifier.accept(meta);

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            lore = Collections.singletonList("InvSee++ placeholder");
        } else if (!"InvSee++ placeholder".equals(lore.get(lore.size() - 1))) {
            lore.add("");
            lore.add("InvSee++ placeholder");
        }
        meta.setLore(lore);

        itemStack.setItemMeta(meta);
    }

    public static <IM extends ItemMeta> Consumer<IM> name(String name) {
        return meta -> meta.setDisplayName(name);
    }

    public static <IM extends ItemMeta> Consumer<IM> and(Consumer<? super IM>... modifiers) {
        return meta -> { for (Consumer<? super IM> modifier : modifiers) modifier.accept(meta); };
    }

}
