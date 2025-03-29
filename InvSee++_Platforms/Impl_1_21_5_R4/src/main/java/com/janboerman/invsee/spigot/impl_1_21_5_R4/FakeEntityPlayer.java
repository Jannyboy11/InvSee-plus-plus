package com.janboerman.invsee.spigot.impl_1_21_5_R4;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

class FakeEntityPlayer extends ServerPlayer {

    private FakeCraftPlayer bukkitEntity;

    public FakeEntityPlayer(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile, ClientInformation clientinformation) {
        super(minecraftserver, worldserver, gameprofile, clientinformation);
    }

    @Override
    public FakeCraftPlayer getBukkitEntity() {
        if (bukkitEntity == null) {
            bukkitEntity = new FakeCraftPlayer(super.getServer().server, this);

            try {
                //https://github.com/Jannyboy11/InvSee-plus-plus/issues/72
                //make sure we set bukkitEntity, to ensure that CraftBukkit can get the PersistentData from the CraftEntity when saving the player's NBT tag compound!
                //See Entity#saveWithoutId
                Field craftbukkitField = Entity.class.getDeclaredField("bukkitEntity");
                craftbukkitField.setAccessible(true);
                craftbukkitField.set(this, bukkitEntity);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, "Failed to overwrite CraftBukkit's 'bukkitEntity'.", e);
            }
        }

        return bukkitEntity;
    }
}
