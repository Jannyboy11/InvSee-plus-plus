package com.janboerman.invsee.spigot.impl_1_12_R1;

import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.InventoryEnderChest;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NonNullList;
import net.minecraft.server.v1_12_R1.Slot;
import net.minecraft.server.v1_12_R1.WorldNBTStorage;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public class HybridServerSupport {

    private HybridServerSupport() {}

    //not really needed on Magma 1.12.2
    public static File getPlayerDir(WorldNBTStorage worldNBTStorage) {
        try {
            return worldNBTStorage.getPlayerDir();
        } catch (NoSuchMethodError craftbukkitMethodNotFound) {
            RuntimeException ex = new RuntimeException("No method known of getting the player directory");
            ex.addSuppressed(craftbukkitMethodNotFound);
            throw ex;
        }
    }

    //not really needed on Magma 1.12.2
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
        } catch (IllegalAccessError craftbukkitFieldIsActuallyPrivate) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle methodHandle = lookup.findVirtual(slot.getClass(), "getSlotIndex", MethodType.methodType(int.class));
                //this should work on Magma as well as Mohist.
                return (int) methodHandle.invoke(slot);
            } catch (Throwable forgeMethodNotFound) {
                RuntimeException ex = new RuntimeException("No method known of getting the slot's inventory index");
                ex.addSuppressed(craftbukkitFieldIsActuallyPrivate);
                ex.addSuppressed(forgeMethodNotFound);
                throw ex;
            }
        }
    }

    public static NonNullList<ItemStack> enderChestItems(InventoryEnderChest enderChest) {
        try {
            return enderChest.items;
        } catch (NoSuchFieldError craftbukkitFildIsActuallyPrivate) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle methodHandle = lookup.findVirtual(enderChest.getClass(), "getContents", MethodType.methodType(List.class));
                return (NonNullList<ItemStack>) methodHandle.invoke(enderChest);
            } catch (Throwable forgeMethodNotFound) {
                RuntimeException ex = new RuntimeException("No method known of getting the enderchest items");
                ex.addSuppressed(craftbukkitFildIsActuallyPrivate);
                ex.addSuppressed(forgeMethodNotFound);
                throw ex;
            }
        }
    }

}
