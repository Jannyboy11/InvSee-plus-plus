package com.janboerman.invsee.spigot.impl_1_19_2_R1;

import com.janboerman.invsee.utils.FuzzyReflection;
import net.minecraft.world.level.storage.PlayerDataStorage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlayerDirectory {

    private PlayerDirectory() {}

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

}
