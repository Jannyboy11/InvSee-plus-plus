package com.janboerman.invsee.spigot.impl_1_20_5_R4;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Multimap;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;

class MaxStackMeta implements ItemMeta, Damageable {

    Integer maxStack = null;

    @Override
    public boolean hasDisplayName() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public void setDisplayName(String s) {

    }

    @Override
    public boolean hasItemName() {
        return false;
    }

    @Override
    public String getItemName() {
        return "";
    }

    @Override
    public void setItemName(String s) {

    }

    @Override
    public boolean hasLocalizedName() {
        return false;
    }

    @Override
    public String getLocalizedName() {
        return "";
    }

    @Override
    public void setLocalizedName(String s) {

    }

    @Override
    public boolean hasLore() {
        return false;
    }

    @Override
    public List<String> getLore() {
        return List.of();
    }

    @Override
    public void setLore(List<String> list) {

    }

    @Override
    public boolean hasCustomModelData() {
        return false;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public void setCustomModelData(Integer integer) {

    }

    @Override
    public boolean hasEnchants() {
        return false;
    }

    @Override
    public boolean hasEnchant(Enchantment enchantment) {
        return false;
    }

    @Override
    public int getEnchantLevel(Enchantment enchantment) {
        return 0;
    }

    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return Map.of();
    }

    @Override
    public boolean addEnchant(Enchantment enchantment, int i, boolean b) {
        return false;
    }

    @Override
    public boolean removeEnchant(Enchantment enchantment) {
        return false;
    }

    @Override
    public void removeEnchantments() {

    }

    @Override
    public boolean hasConflictingEnchant(Enchantment enchantment) {
        return false;
    }

    @Override
    public void addItemFlags(ItemFlag... itemFlags) {

    }

    @Override
    public void removeItemFlags(ItemFlag... itemFlags) {

    }

    @Override
    public Set<ItemFlag> getItemFlags() {
        return Set.of();
    }

    @Override
    public boolean hasItemFlag(ItemFlag itemFlag) {
        return false;
    }

    @Override
    public boolean isHideTooltip() {
        return false;
    }

    @Override
    public void setHideTooltip(boolean b) {

    }

    @Override
    public boolean isUnbreakable() {
        return false;
    }

    @Override
    public void setUnbreakable(boolean b) {

    }

    @Override
    public boolean hasEnchantmentGlintOverride() {
        return false;
    }

    @Override
    public Boolean getEnchantmentGlintOverride() {
        return null;
    }

    @Override
    public void setEnchantmentGlintOverride(Boolean aBoolean) {

    }

    @Override
    public boolean isFireResistant() {
        return false;
    }

    @Override
    public void setFireResistant(boolean b) {

    }

    @Override
    public boolean hasMaxStackSize() {
        return maxStack != null;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack.intValue();
    }

    @Override
    public void setMaxStackSize(Integer integer) {
        maxStack = integer;
    }

    @Override
    public boolean hasRarity() {
        return false;
    }

    @Override
    public ItemRarity getRarity() {
        return null;
    }

    @Override
    public void setRarity(ItemRarity itemRarity) {

    }

    @Override
    public boolean hasFood() {
        return false;
    }

    @Override
    public FoodComponent getFood() {
        return null;
    }

    @Override
    public void setFood(FoodComponent foodComponent) {

    }

    @Override
    public boolean hasAttributeModifiers() {
        return false;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        return null;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot) {
        return null;
    }

    @Override
    public Collection<AttributeModifier> getAttributeModifiers(Attribute attribute) {
        return List.of();
    }

    @Override
    public boolean addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        return false;
    }

    @Override
    public void setAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {

    }

    @Override
    public boolean removeAttributeModifier(Attribute attribute) {
        return false;
    }

    @Override
    public boolean removeAttributeModifier(EquipmentSlot equipmentSlot) {
        return false;
    }

    @Override
    public boolean removeAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        return false;
    }

    @Override
    public String getAsString() {
        return "";
    }

    @Override
    public CustomItemTagContainer getCustomTagContainer() {
        return null;
    }

    @Override
    public void setVersion(int i) {

    }

    @Override
    public boolean hasDamage() {
        return false;
    }

    @Override
    public int getDamage() {
        return 0;
    }

    @Override
    public void setDamage(int i) {

    }

    @Override
    public boolean hasMaxDamage() {
        return false;
    }

    @Override
    public int getMaxDamage() {
        return 0;
    }

    @Override
    public void setMaxDamage(Integer integer) {

    }

    @Override
    public MaxStackMeta clone() {
        return this;
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of();
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) || (o instanceof MaxStackMeta that && Objects.equals(this.maxStack, that.maxStack));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maxStack);
    }
}
