package com.janboerman.invsee.spigot.impl_1_20_1_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

class FakeEntityPlayer extends ServerPlayer {

    private FakeCraftPlayer bukkitEntity;

    FakeEntityPlayer(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile) {
        super(minecraftserver, worldserver, gameprofile);
    }

    @Override
    public FakeCraftPlayer getBukkitEntity() {
        return bukkitEntity == null ? bukkitEntity = new FakeCraftPlayer(super.getServer().server, this) : bukkitEntity;
    }
}
