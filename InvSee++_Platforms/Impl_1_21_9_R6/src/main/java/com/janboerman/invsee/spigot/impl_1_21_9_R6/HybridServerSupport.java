package com.janboerman.invsee.spigot.impl_1_21_9_R6;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import com.janboerman.invsee.utils.FuzzyReflection;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Since MC 1.21.6, this class is not only used to patch up differences between CraftBukkit-based server and Forge-based servers,
 * but also differences between CraftBukkit and Paper.
 */
public final class HybridServerSupport {

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
                //this should work on Mohist.
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
        } catch (NoSuchFieldError | IllegalAccessError vanillaFieldIsActuallyPrivate) {
            try {
                //call the forge method: getContents()Ljava/util/List<net/minecraft/world/item/ItemStack>;
                //fortunately CraftBukkit contains this method as well, so we can just call it directly without reflection! :D
                return (NonNullList<ItemStack>) enderChest.getContents();
            } catch (Throwable forgeMethodNotFound) {
                RuntimeException ex = new RuntimeException("No method known of getting the enderchest items");
                ex.addSuppressed(vanillaFieldIsActuallyPrivate);
                ex.addSuppressed(forgeMethodNotFound);
                throw ex;
            }
        }
    }

    public static Optional<ValueInput> load(PlayerDataStorage playerIO, String name, String uuid, ProblemReporter problemReporter, RegistryAccess registryAccess) {
        try {
            return playerIO.load(new NameAndId(UUID.fromString(uuid), name))
                    .map(compoundTag -> TagValueInput.create(problemReporter, registryAccess, compoundTag));
        } catch (NoSuchMethodError craftbukkitMethodNotFound) {
            //call the paper method: load(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/util/ProblemReporter;)Ljava/util/Optional
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                MethodHandle methodHandle = lookup.findVirtual(PlayerDataStorage.class, "load", MethodType.methodType(Optional.class, String.class, String.class, ProblemReporter.class));
                Optional<CompoundTag> optionalCompoundTag = (Optional<CompoundTag>) methodHandle.invoke(playerIO, name, uuid, problemReporter);
                return optionalCompoundTag.map(compoundTag -> TagValueInput.create(problemReporter, registryAccess, compoundTag));
            } catch (Throwable paperMethodNotFound) {
                RuntimeException ex = new RuntimeException("No method known of loading the player's data file");
                ex.addSuppressed(craftbukkitMethodNotFound);
                ex.addSuppressed(paperMethodNotFound);
                throw ex;
            }
        }
    }

}
