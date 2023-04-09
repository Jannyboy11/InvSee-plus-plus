package com.janboerman.invsee.spigot.api.logging;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * An item type is a product of {@link Material} and {@link ItemMeta}.
 */
public class ItemType {

    private final Material material;
    private final ItemMeta meta;

    /**
     * Construct the ItemType
     * @param material the material
     * @param meta the item meta
     */
    public ItemType(Material material, ItemMeta meta) {
        this.material = material;
        this.meta = meta;
    }

    /**
     * Construct the ItemType
     * @param stack the stack to obtain the Material and ItemMeta from.
     * @return a new ItemType, or null if the item stack is empty
     */
    public static ItemType of(ItemStack stack) {
        if (isEmptyStack(stack)) return null;

        return new ItemType(stack.getType(), stack.hasItemMeta() ? stack.getItemMeta() : null);
    }

    /**
     * Get the material of this item type.
     * @return the material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Get the item meta of this item type.
     * @return the item meta
     */
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
