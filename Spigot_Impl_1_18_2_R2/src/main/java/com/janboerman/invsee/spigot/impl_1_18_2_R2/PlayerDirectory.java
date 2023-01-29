package com.janboerman.invsee.spigot.impl_1_18_2_R2;

import com.janboerman.invsee.utils.FuzzyReflection;
import net.minecraft.world.level.storage.PlayerDataStorage;

import java.io.File;
import java.lang.reflect.Field;

public class PlayerDirectory {

    private PlayerDirectory() {}

    public static File getPlayerDir(PlayerDataStorage worldNBTStorage) throws Exception {
        try {
            return worldNBTStorage.getPlayerDir();
        } catch (NoSuchMethodError moddedServer) {
            Field[] fields = FuzzyReflection.getFieldOfType(PlayerDataStorage.class, File.class);
            for (Field field : fields) {
                File file = (File) field.get(worldNBTStorage);
                if (file.isDirectory()) {
                    return file;
                }
            }
            RuntimeException re = new RuntimeException("No method known of getting the player directory");
            re.addSuppressed(moddedServer);
            throw re;
        }
    }

}
