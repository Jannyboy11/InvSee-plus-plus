package com.janboerman.invsee.fakes;

import com.destroystokyo.paper.util.VersionFetcher;
import com.google.common.collect.Multimap;
import io.papermc.paper.inventory.ItemRarity;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

public class FakeUnsafeValues implements UnsafeValues {

    static final FakeUnsafeValues INSTANCE = new FakeUnsafeValues();

    private FakeUnsafeValues() {
    }

    @Override
    public ComponentFlattener componentFlattener() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlainComponentSerializer plainComponentSerializer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlainTextComponentSerializer plainTextSerializer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GsonComponentSerializer gsonComponentSerializer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GsonComponentSerializer colorDownsamplingGsonComponentSerializer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LegacyComponentSerializer legacyComponentSerializer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reportTimings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Material toLegacy(Material material) {
        return material;
    }

    @Override
    public Material fromLegacy(Material material) {
        return material;
    }

    @Override
    public Material fromLegacy(MaterialData materialData) {
        return materialData.getItemType();
    }

    @Override
    public Material fromLegacy(MaterialData materialData, boolean b) {
        return materialData.getItemType();
    }

    @Override
    public BlockData fromLegacy(Material material, byte b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Material getMaterial(String s, int i) {
        return Material.getMaterial(s);
    }

    @Override
    public int getDataVersion() {
        return 0;   //probably inaccurate. but w/e

        //CraftBukkit implements this using the following line:
        //SharedConstants.getGameVersion().getWorldVersion();
    }

    @Override
    public ItemStack modifyItemStack(ItemStack itemStack, String s) {
        //s is a json string that represents an nbt tag
        return itemStack;
    }

    @Override
    public void checkSupported(PluginDescriptionFile pluginDescriptionFile) throws InvalidPluginException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] processClass(PluginDescriptionFile pluginDescriptionFile, String s, byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Advancement loadAdvancement(NamespacedKey namespacedKey, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAdvancement(NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTimingsServerName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VersionFetcher getVersionFetcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupportedApiVersion(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] serializeItem(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack deserializeItem(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] serializeEntity(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity deserializeEntity(byte[] data, World world) {
        return UnsafeValues.super.deserializeEntity(data, world);
    }

    @Override
    public Entity deserializeEntity(byte[] bytes, World world, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTranslationKey(Material material) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTranslationKey(Block block) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTranslationKey(EntityType entityType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTranslationKey(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int nextEntityId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemRarity getItemRarity(Material material) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemRarity getItemStackRarity(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidRepairItemStack(@NotNull ItemStack itemStack, @NotNull ItemStack itemStack1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getItemAttributes(@NotNull Material material, @NotNull EquipmentSlot equipmentSlot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getProtocolVersion() {
        //CraftUnsafeValues
        return 0;
    }

    @Override
    public boolean hasDefaultEntityAttributes(@NotNull NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Attributable getDefaultEntityAttributes(@NotNull NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull Material material, @NotNull EquipmentSlot equipmentSlot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCollidable(@NotNull Material material) {
        throw new UnsupportedOperationException();
    }

}
