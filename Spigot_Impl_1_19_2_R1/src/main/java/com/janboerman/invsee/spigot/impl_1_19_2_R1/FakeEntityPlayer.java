package com.janboerman.invsee.spigot.impl_1_19_2_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;

public class FakeEntityPlayer extends ServerPlayer {

    private FakeCraftPlayer bukkitEntity;

    public FakeEntityPlayer(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile, ProfilePublicKey profilePublicKey) {
        super(minecraftserver, worldserver, gameprofile, profilePublicKey);
    }

    @Override
    public FakeCraftPlayer getBukkitEntity() {
        return bukkitEntity == null ? bukkitEntity = new FakeCraftPlayer(level.getCraftServer(), this) : bukkitEntity;
    }
}
