package com.janboerman.invsee.spigot.impl_1_15_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.PlayerInteractManager;
import net.minecraft.server.v1_15_R1.WorldServer;

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
