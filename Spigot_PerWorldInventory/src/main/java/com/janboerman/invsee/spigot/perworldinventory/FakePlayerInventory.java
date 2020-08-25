package com.janboerman.invsee.spigot.perworldinventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class FakePlayerInventory extends FakeInventory implements PlayerInventory, EntityEquipment {

    private float itemInHandDropChance, itemInMainHandDropChance, itemInOffHandDropChance;
    private float helmetDropChance, chestPlateDropChance, leggingsDropChance, bootsDropChance;
    {
        itemInHandDropChance = itemInMainHandDropChance = itemInOffHandDropChance = 1F;
        helmetDropChance = chestPlateDropChance = leggingsDropChance = bootsDropChance = 1F;
    }

    private int mainHandIndex;

    public FakePlayerInventory(ItemStack[] items, HumanEntity player) {
        super(InventoryType.PLAYER, items, player);
        assert items.length == 41 : "player inventory items length must equal 41!";
    }

    @Override
    public ItemStack[] getStorageContents() {
        return Arrays.copyOf(items, 36);
    }

    @Override
    public void setStorageContents(ItemStack[] itemStacks) {
        System.arraycopy(itemStacks, 0, items, 0, 36);
    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return items[mainHandIndex];
    }

    @Override
    public void setItemInMainHand(@Nullable ItemStack itemStack) {
        items[mainHandIndex] = itemStack;
    }

    @NotNull
    @Override
    public ItemStack[] getArmorContents() {
        return Arrays.copyOfRange(items, 36, 40);
    }

    @NotNull
    @Override
    public ItemStack[] getExtraContents() {
        return Arrays.copyOfRange(items, 40, 41);
    }

    @Override
    public void setExtraContents(@Nullable ItemStack[] itemStacks) {
        System.arraycopy(itemStacks, 0, items, 40, 1);
    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return items[40];
    }

    @Override
    public void setItemInOffHand(@Nullable ItemStack itemStack) {
        items[40] = itemStack;
    }

    @Nullable
    @Override
    public ItemStack getHelmet() {
        return items[39];
    }

    @Override
    public void setHelmet(@Nullable ItemStack itemStack) {
        items[39] = itemStack;
    }

    @Nullable
    @Override
    public ItemStack getChestplate() {
        return items[38];
    }

    @Override
    public void setChestplate(@Nullable ItemStack itemStack) {
        items[38] = itemStack;
    }

    @Nullable
    @Override
    public ItemStack getLeggings() {
        return items[37];
    }


    @Override
    public void setLeggings(@Nullable ItemStack itemStack) {
        items[37] = itemStack;
    }

    @Nullable
    @Override
    public ItemStack getBoots() {
        return items[36];
    }

    @Override
    public void setBoots(@Nullable ItemStack itemStack) {
        items[36] = itemStack;
    }

    @Override
    public void setItem(@NotNull EquipmentSlot equipmentSlot, @Nullable ItemStack itemStack) {
        switch (equipmentSlot) {
            case FEET: setBoots(itemStack); break;
            case LEGS: setLeggings(itemStack); break;
            case CHEST: setChestplate(itemStack); break;
            case HEAD: setHelmet(itemStack); break;
            case OFF_HAND: setItemInOffHand(itemStack); break;
            case HAND: setItemInMainHand(itemStack); break;
        }
    }

    @NotNull
    @Override
    public ItemStack getItem(@NotNull EquipmentSlot equipmentSlot) {
        switch (equipmentSlot) {
            case FEET: return getBoots();
            case LEGS: return getLeggings();
            case CHEST: return getChestplate();
            case HEAD: return getHelmet();
            case OFF_HAND: return getItemInOffHand();
            case HAND: return getItemInMainHand();
        }
        return null; //can't use switch expressions yet in Java 11 :/
    }

    @Override
    public void setArmorContents(@Nullable ItemStack[] itemStacks) {
        System.arraycopy(itemStacks, 0, items, 36, 4);
    }

    @NotNull
    @Override
    public ItemStack getItemInHand() {
        return getItemInMainHand();
    }

    @Override
    public void setItemInHand(@Nullable ItemStack itemStack) {
        setItemInMainHand(itemStack);
    }

    @Override
    public int getHeldItemSlot() {
        return mainHandIndex;
    }

    @Override
    public void setHeldItemSlot(int i) {
        if (i < 0 || i >= 9)
            throw new IllegalArgumentException("held item slot must be in range of 0-8 (inclusive)");

        mainHandIndex = i;
    }

    @Override
    public void forEach(Consumer<? super ItemStack> action) {
        for (ItemStack itemStack : items) {
            action.accept(itemStack);
        }
    }

    @Override
    public Spliterator<ItemStack> spliterator() {
        return Spliterators.spliterator(items, Spliterator.ORDERED);
    }

    @Override
    public HumanEntity getHolder() {
        return (HumanEntity) super.getHolder();
    }

    @Override
    public float getItemInHandDropChance() {
        return itemInHandDropChance;
    }

    @Override
    public void setItemInHandDropChance(float v) {
        this.itemInHandDropChance = v;
        setItemInMainHandDropChance(v);
        setItemInOffHandDropChance(v);
    }

    @Override
    public float getItemInMainHandDropChance() {
        return itemInMainHandDropChance;
    }

    @Override
    public void setItemInMainHandDropChance(float v) {
        this.itemInMainHandDropChance = v;
    }

    @Override
    public float getItemInOffHandDropChance() {
        return itemInOffHandDropChance;
    }

    @Override
    public void setItemInOffHandDropChance(float v) {
        this.itemInOffHandDropChance = v;
    }

    @Override
    public float getHelmetDropChance() {
        return helmetDropChance;
    }

    @Override
    public void setHelmetDropChance(float v) {
        this.helmetDropChance = v;
    }

    @Override
    public float getChestplateDropChance() {
        return chestPlateDropChance;
    }

    @Override
    public void setChestplateDropChance(float v) {
        this.chestPlateDropChance = v;
    }

    @Override
    public float getLeggingsDropChance() {
        return leggingsDropChance;
    }

    @Override
    public void setLeggingsDropChance(float v) {
        this.leggingsDropChance = v;
    }

    @Override
    public float getBootsDropChance() {
        return bootsDropChance;
    }

    @Override
    public void setBootsDropChance(float v) {
        this.bootsDropChance = v;
    }
}
