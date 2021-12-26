package com.janboerman.invsee.spigot.impl_1_18_1_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class FakeEntityPlayer extends ServerPlayer {
    public FakeEntityPlayer(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile) {
        super(minecraftserver, worldserver, gameprofile);
    }

    @Override
    public FakeCraftPlayer getBukkitEntity() {
        return new FakeCraftPlayer(level.getCraftServer(), this);
    }
}
