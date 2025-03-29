package com.janboerman.invsee.spigot.impl_1_21_5_R4;

import java.util.Objects;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class FakeItemFactory implements ItemFactory {
    @Override
    public ItemMeta getItemMeta(Material material) {
        return new MaxStackMeta();
    }

    @Override
    public boolean isApplicable(ItemMeta itemMeta, ItemStack itemStack) throws IllegalArgumentException {
        return true;
    }

    @Override
    public boolean isApplicable(ItemMeta itemMeta, Material material) throws IllegalArgumentException {
        return true;
    }

    @Override
    public boolean equals(ItemMeta itemMeta, ItemMeta itemMeta1) throws IllegalArgumentException {
        return Objects.equals(itemMeta, itemMeta1);
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta itemMeta, ItemStack itemStack) throws IllegalArgumentException {
        return itemMeta;
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta itemMeta, Material material) throws IllegalArgumentException {
        return itemMeta;
    }

    @Override
    public Color getDefaultLeatherColor() {
        return null;
    }

    @Override
    public ItemStack createItemStack(String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Material getSpawnEgg(EntityType entityType) {
        return null;
    }

    @Override
    public ItemStack enchantItem(Entity entity, ItemStack itemStack, int i, boolean b) {
        return itemStack;
    }

    @Override
    public ItemStack enchantItem(World world, ItemStack itemStack, int i, boolean b) {
        return itemStack;
    }

    @Override
    public ItemStack enchantItem(ItemStack itemStack, int i, boolean b) {
        return itemStack;
    }
}
