package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.WorldServer;
import net.minecraft.server.v1_16_R3.EntityPlayer;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FakeEntityPlayer extends EntityPlayer {

    private FakeCraftPlayer bukkitEntity;

    public FakeEntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerInteractManager) {
        super(minecraftserver, worldserver, gameprofile, playerInteractManager);
    }

    @Override
    public FakeCraftPlayer getBukkitEntity() {
        if (bukkitEntity == null) {
            bukkitEntity = new FakeCraftPlayer(super.world.getServer(), this);

            try {
                //https://github.com/Jannyboy11/InvSee-plus-plus/issues/72
                //make sure we set bukkitEntity, to ensure that CraftBukkit can get the PersistentData from the CraftEntity when saving the player's NBT tag compound!
                //See Entity#save
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
