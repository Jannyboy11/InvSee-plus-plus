package com.janboerman.invsee.spigot.impl_1_8_R3;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.InventoryEnderChest;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Slot;
import net.minecraft.server.v1_8_R3.WorldNBTStorage;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public class HybridServerSupport {

    private HybridServerSupport() {}

    public static File getPlayerDir(WorldNBTStorage worldNBTStorage) {
        try {
            return worldNBTStorage.getPlayerDir();
        } catch (NoSuchMethodError craftbukkitMethodNotFound) {
            RuntimeException ex = new RuntimeException("No method known of getting the player directory");
            ex.addSuppressed(craftbukkitMethodNotFound);
            throw ex;
        }
    }

    public static int nextContainerCounter(EntityPlayer nmsPlayer) {
        try {
            return nmsPlayer.nextContainerCounter();
        } catch (NoSuchMethodError craftbukkitMethodNotFound) {
            //look up the obfuscated field name and get it by reflection?
            RuntimeException ex = new RuntimeException("No method known of incrementing the player's container counter");
            ex.addSuppressed(craftbukkitMethodNotFound);
            throw ex;
        }
    }

    public static int slot(Slot slot) {
        try {
            return slot.index;
        } catch (NoSuchFieldError | IllegalAccessError craftbukkitFieldIsActuallyPrivate) {
            try {
                //call the forge method: getSlotIndex()I
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle methodHandle = lookup.findVirtual(slot.getClass(), "getSlotIndex", MethodType.methodType(int.class));
                return (int) methodHandle.invoke(slot);
            } catch (Throwable forgeMethodNotFound) {
                RuntimeException ex = new RuntimeException("No method known of getting the slot's inventory index");
                ex.addSuppressed(craftbukkitFieldIsActuallyPrivate);
                ex.addSuppressed(forgeMethodNotFound);
                throw ex;
            }
        }
    }

    public static ItemStack[] enderChestItems(InventoryEnderChest enderChest) {
        try {
            return enderChest.items;
        } catch (NoSuchFieldError | IllegalAccessError craftbukkitFieldIsActuallyPrivate) {
            try {
                //call the forge method: getContents()[Lnet/minecraft/server/v1_8_R3/ItemStack;
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle methodHandle = lookup.findVirtual(enderChest.getClass(), "getContents", MethodType.methodType(ItemStack[].class));
                return (ItemStack[]) methodHandle.invoke(enderChest);
            } catch (Throwable forgeMethodNotFound) {
                RuntimeException ex = new RuntimeException("No method known of getting the enderchest items");
                ex.addSuppressed(craftbukkitFieldIsActuallyPrivate);
                ex.addSuppressed(forgeMethodNotFound);
                throw ex;
            }
        }
    }

}
