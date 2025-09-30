package com.janboerman.invsee.spigot.impl_1_21_9_R6;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

class FakeEntityPlayer extends ServerPlayer {

    private static final Method SET_GAME_MODE_FOR_PLAYER = findSetGameModeForPlayer();

    private FakeCraftPlayer bukkitEntity;

    public FakeEntityPlayer(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile, ClientInformation clientinformation) {
        super(minecraftserver, worldserver, gameprofile, clientinformation);
    }

    @Override
    public FakeCraftPlayer getBukkitEntity() {
        if (bukkitEntity == null) {
            bukkitEntity = new FakeCraftPlayer(super.server.server, this);

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

    void setGameMode(GameType gameMode, GameType previousGameMode) {
        if (SET_GAME_MODE_FOR_PLAYER != null) {
            try {
                SET_GAME_MODE_FOR_PLAYER.invoke(this.gameMode, gameMode, previousGameMode);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Could not set gamemode for player.", e);
            }
        }
    }

    private static Method findSetGameModeForPlayer() {
        Class<?>[] expectedParameterTypes = new Class<?>[] { GameType.class, GameType.class };
        for (Method m : ServerPlayerGameMode.class.getDeclaredMethods()) {
            if (Arrays.equals(m.getParameterTypes(), expectedParameterTypes)) {
                m.setAccessible(true);
                return m;
            }
        }
        Logger.getLogger("Minecraft").warning("Failed to find method ServerPlayerGameMode#setGameModeForPlayer(GameType,GameType).");
        return null;
    }

}
