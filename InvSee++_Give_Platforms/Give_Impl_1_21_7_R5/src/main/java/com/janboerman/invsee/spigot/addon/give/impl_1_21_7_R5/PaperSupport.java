package com.janboerman.invsee.spigot.addon.give.impl_1_21_7_R5;

import org.bukkit.craftbukkit.v1_21_R5.CraftRegistry;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;

public final class PaperSupport {

    private PaperSupport() {
    }

    public static RegistryAccess getDefaultRegistry() {
        try {
            return MinecraftServer.getDefaultRegistryAccess();
        } catch (NoSuchMethodError craftBukkitMethodNotFound) {
            try {
                return CraftRegistry.getMinecraftRegistry();
            } catch (NoSuchMethodError paperMethodNotFound) {
                RuntimeException ex = new RuntimeException("No method known of obtaining the default registry");
                ex.addSuppressed(craftBukkitMethodNotFound);
                ex.addSuppressed(paperMethodNotFound);
                throw ex;
            }
        }
    }

}
