package com.janboerman.invsee.spigot.impl_1_19_4_R3;

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
        return bukkitEntity == null ? bukkitEntity = new FakeCraftPlayer(super.level.getCraftServer(), this) : bukkitEntity;    // Mohist compat: use super.level instead of this.level
    }
}
