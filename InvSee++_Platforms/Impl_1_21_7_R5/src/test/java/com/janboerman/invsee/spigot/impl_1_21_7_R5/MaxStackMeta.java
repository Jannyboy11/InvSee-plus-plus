package com.janboerman.invsee.spigot.impl_1_21_7_R5;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Multimap;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.BlocksAttacksComponent;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.inventory.meta.components.WeaponComponent;
import org.bukkit.inventory.meta.components.consumable.ConsumableComponent;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull CustomModelDataComponent getCustomModelDataComponent() {
        return null;
    }

    @Override
    public void setCustomModelData(Integer integer) {

    }

    @Override
    public boolean hasCustomModelDataComponent() {
        return false;
    }

    @Override
    public void setCustomModelDataComponent(CustomModelDataComponent customModelDataComponent) {

    }

    @Override
    public boolean hasEnchantable() {
        return false;
    }

    @Override
    public int getEnchantable() {
        return 0;
    }

    @Override
    public void setEnchantable(Integer integer) {

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
    public boolean hasTooltipStyle() {
        return false;
    }

    @Override
    public NamespacedKey getTooltipStyle() {
        return null;
    }

    @Override
    public void setTooltipStyle(NamespacedKey namespacedKey) {

    }

    @Override
    public boolean hasItemModel() {
        return false;
    }

    @Override
    public NamespacedKey getItemModel() {
        return null;
    }

    @Override
    public void setItemModel(NamespacedKey namespacedKey) {

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
    public boolean isGlider() {
        return false;
    }

    @Override
    public void setGlider(boolean b) {

    }

    @Override
    public boolean isFireResistant() {
        return false;
    }

    @Override
    public void setFireResistant(boolean b) {

    }

    @Override
    public boolean hasDamageResistant() {
        return false;
    }

    @Override
    public Tag<DamageType> getDamageResistant() {
        return null;
    }

    @Override
    public void setDamageResistant(Tag<DamageType> tag) {

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
    public boolean hasUseRemainder() {
        return false;
    }

    @Override
    public ItemStack getUseRemainder() {
        return null;
    }

    @Override
    public void setUseRemainder(ItemStack itemStack) {

    }

    @Override
    public boolean hasUseCooldown() {
        return false;
    }

    @Override
    public UseCooldownComponent getUseCooldown() {
        return null;
    }

    @Override
    public void setUseCooldown(UseCooldownComponent useCooldownComponent) {

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
    public boolean hasConsumable() {
        return false;
    }

    @Override
    public @NotNull ConsumableComponent getConsumable() {
        return null;
    }

    @Override
    public void setConsumable(ConsumableComponent consumableComponent) {

    }

    @Override
    public boolean hasTool() {
        return false;
    }

    @Override
    public ToolComponent getTool() {
        return null;
    }

    @Override
    public void setTool(ToolComponent toolComponent) {

    }

    @Override
    public boolean hasWeapon() {
        return false;
    }

    @Override
    public @NotNull WeaponComponent getWeapon() {
        return null;
    }

    @Override
    public void setWeapon(WeaponComponent weaponComponent) {

    }

    @Override
    public boolean hasBlocksAttacks() {
        return false;
    }

    @Override
    public @NotNull BlocksAttacksComponent getBlocksAttacks() {
        return null;
    }

    @Override
    public void setBlocksAttacks(BlocksAttacksComponent blocksAttacksComponent) {

    }

    @Override
    public boolean hasEquippable() {
        return false;
    }

    @Override
    public EquippableComponent getEquippable() {
        return null;
    }

    @Override
    public void setEquippable(EquippableComponent equippableComponent) {

    }

    @Override
    public boolean hasJukeboxPlayable() {
        return false;
    }

    @Override
    public JukeboxPlayableComponent getJukeboxPlayable() {
        return null;
    }

    @Override
    public void setJukeboxPlayable(JukeboxPlayableComponent jukeboxPlayableComponent) {

    }

    @Override
    public boolean hasBreakSound() {
        return false;
    }

    @Override
    public Sound getBreakSound() {
        return null;
    }

    @Override
    public void setBreakSound(Sound sound) {

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
    public String getAsComponentString() {
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
        MaxStackMeta clone = new MaxStackMeta();
        clone.maxStack = this.maxStack;
        return clone;
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
