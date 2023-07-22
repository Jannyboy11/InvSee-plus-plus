package com.janboerman.invsee.spigot.internal.placeholder;

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

    public static final String INACCESSIBLE = "";
    public static final String HELMET = "Helmet";
    public static final String CHESTPLATE = "Chestplate";
    public static final String LEGGINGS = "Leggings";
    public static final String BOOTS = "Boots";
    public static final String OFFHAND = "Off-hand";
    public static final String CURSOR = "Cursor";
    public static final String CRAFTING = "Crafting ingredient";
    public static final String ANVIL = "Anvil input";
    public static final String MERCHANT = "Merchant payment";
    public static final String CARTOGRAPHY = "Cartography input";
    public static final String ENCHANTING_ITEM = "Enchanting item";
    public static final String ENCHANTING_FUEL = "Enchanting lapis lazuli";
    public static final String GRINDSTONE = "Grindstone input";
    public static final String LOOM = "Loom input";
    public static final String SMITHING_BASE = "Smithing item";
    public static final String SMITHING_TEMPLATE = "Smithing template";
    public static final String SMITHING_ADDITION = "Smithing addition";
    public static final String STONECUTTER = "Stonecutter input";
    public static final String GENERIC = "";

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
