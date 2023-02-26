package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.utils.FuzzyReflection;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.InventoryEnderChest;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.NonNullList;
import net.minecraft.server.v1_16_R3.Slot;
import net.minecraft.server.v1_16_R3.WorldNBTStorage;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class HybridServerSupport {

    private HybridServerSupport() {}

    public static File getPlayerDir(WorldNBTStorage worldNBTStorage) {
        try {
            return worldNBTStorage.getPlayerDir();
        } catch (NoSuchMethodError craftbukkitMethodNotFound) {
            try {
                Method method = worldNBTStorage.getClass().getMethod("getPlayerDataFolder");
                return (File) method.invoke(worldNBTStorage);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException forgeMethodNotFound) {
                Field[] fields = FuzzyReflection.getFieldOfType(WorldNBTStorage.class, File.class);
                for (Field field : fields) {
                    try {
                        return (File) field.get(worldNBTStorage);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Could not access player data folder field from PlayerDataStorage", e);
                    }
                }
                RuntimeException ex = new RuntimeException("No method known of getting the player directory");
                ex.addSuppressed(craftbukkitMethodNotFound);
                ex.addSuppressed(forgeMethodNotFound);
                throw ex;
            }
        }
    }

    public static int nextContainerCounter(EntityPlayer nmsPlayer) {
        try {
            return nmsPlayer.nextContainerCounter();
        } catch (NoSuchMethodError craftbukkitMethodNotFound) {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                MethodHandle methodHandle = lookup.findVirtual(nmsPlayer.getClass(), "nextContainerCounterInt", MethodType.methodType(int.class));
                //this should work on Magma as well as Mohist.
                return (int) methodHandle.invoke(nmsPlayer);
            } catch (Throwable magmaMethodNotFound) {
                //look up the obfuscated field name and get it by reflection?
                RuntimeException ex = new RuntimeException("No method known of incrementing the player's container counter");
                ex.addSuppressed(craftbukkitMethodNotFound);
                ex.addSuppressed(magmaMethodNotFound);
                throw ex;
            }
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

    public static NonNullList<ItemStack> enderChestItems(InventoryEnderChest enderChest) {
        try {
            return enderChest.items;
        } catch (NoSuchFieldError | IllegalAccessError craftbukkitFildIsActuallyPrivate) {
            try {
                //call the forge method: getContents()Ljava/util/List<net.minecraft.server.v1_12_R1.ItemStack>;
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
