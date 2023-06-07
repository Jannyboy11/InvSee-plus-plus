package com.janboerman.invsee.spigot.impl_1_19_4_R3;

import com.janboerman.invsee.utils.FuzzyReflection;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.PlayerDataStorage;

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

    public static File getPlayerDir(PlayerDataStorage worldNBTStorage) {
        try {
            return worldNBTStorage.getPlayerDir();
        } catch (NoSuchMethodError craftbukkitMethodNotFound) {
            try {
                Method method = worldNBTStorage.getClass().getMethod("getPlayerDataFolder");
                return (File) method.invoke(worldNBTStorage);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException forgeMethodNotFound) {
                Field[] fields = FuzzyReflection.getFieldOfType(PlayerDataStorage.class, File.class);
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

    public static int nextContainerCounter(ServerPlayer nmsPlayer) {
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

    public static NonNullList<ItemStack> enderChestItems(PlayerEnderChestContainer enderChest) {
        try {
            return enderChest.items;
        } catch (NoSuchFieldError | IllegalAccessError craftbukkitFieldIsActuallyPrivate) {
            try {
                //call the forge method: getContents()Ljava/util/List<net/minecraft/world/item/ItemStack>;
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle methodHandle = lookup.findVirtual(enderChest.getClass(), "getContents", MethodType.methodType(List.class));
                return (NonNullList<ItemStack>) methodHandle.invoke(enderChest);
            } catch (Throwable forgeMethodNotFound) {
                RuntimeException ex = new RuntimeException("No method known of getting the enderchest items");
                ex.addSuppressed(craftbukkitFieldIsActuallyPrivate);
                ex.addSuppressed(forgeMethodNotFound);
                throw ex;
            }
        }
    }

}
