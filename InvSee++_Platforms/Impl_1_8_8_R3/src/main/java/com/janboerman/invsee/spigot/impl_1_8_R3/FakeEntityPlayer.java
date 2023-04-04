package com.janboerman.invsee.spigot.impl_1_8_R3;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;

public class FakeEntityPlayer extends EntityPlayer {

    private FakeCraftPlayer bukkitEntity;

    public FakeEntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerInteractManager) {
        super(minecraftserver, worldserver, gameprofile, playerInteractManager);
    }

    @Override
    public FakeCraftPlayer getBukkitEntity() {
        return bukkitEntity == null ? bukkitEntity = new FakeCraftPlayer(world.getServer(), this) : bukkitEntity;
    }
}
