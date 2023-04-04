package com.janboerman.invsee.spigot.api.logging;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class ItemType {

    private final Material material;
    private final ItemMeta meta;

    public ItemType(Material material, ItemMeta meta) {
        this.material = material;
        this.meta = meta;
    }

    public static ItemType of(ItemStack stack) {
        if (isEmptyStack(stack)) return null;

        return new ItemType(stack.getType(), stack.hasItemMeta() ? stack.getItemMeta() : null);
    }

    public Material getMaterial() {
        return material;
    }

    public ItemMeta getItemMeta() {
        return meta;
    }

    @Override
    public String toString() {
        return "ItemType(material=" + getMaterial() + ",meta=" + getItemMeta() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemType)) return false;

        ItemType that = (ItemType) o;
        return this.getMaterial() == that.getMaterial()
                && Objects.equals(this.getItemMeta(), that.getItemMeta());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMaterial(), getItemMeta());
    }

    private static boolean isEmptyStack(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0;
    }

}
