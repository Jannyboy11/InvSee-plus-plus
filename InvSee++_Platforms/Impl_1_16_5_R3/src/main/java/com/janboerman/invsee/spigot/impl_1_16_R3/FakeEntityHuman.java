package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.World;

class FakeEntityHuman extends EntityHuman {

    FakeEntityHuman(World world, BlockPosition blockposition, float yaw, GameProfile gameprofile) {
        super(world, blockposition, yaw, gameprofile);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

}
