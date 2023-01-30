package com.janboerman.invsee.spigot.impl_1_19_3_R2;

import com.janboerman.invsee.utils.FuzzyReflection;
import net.minecraft.world.level.storage.PlayerDataStorage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PlayerDirectory {

    private PlayerDirectory() {}

    public static File getPlayerDir(PlayerDataStorage worldNBTStorage) throws Exception {
        try {
            return worldNBTStorage.getPlayerDir();
        } catch (NoSuchMethodError craftbukkitMethodNotFound) {
            try {
                Method method = worldNBTStorage.getClass().getMethod("getPlayerDataFolder");
                return (File) method.invoke(worldNBTStorage);
            } catch (NoSuchMethodException forgeMethodNotFound) {
                Field[] fields = FuzzyReflection.getFieldOfType(PlayerDataStorage.class, File.class);
                for (Field field : fields) {
                    return (File) field.get(worldNBTStorage);
                }
                Exception ex = new Exception("No method known of getting the player directory");
                ex.addSuppressed(craftbukkitMethodNotFound);
                ex.addSuppressed(forgeMethodNotFound);
                throw ex;
            }
        }
    }

}
