package com.janboerman.invsee.spigot.impl_1_16_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.World;

class FakeEntityHuman extends EntityHuman {

    FakeEntityHuman(World world, BlockPosition blockposition, GameProfile gameprofile) {
        super(world, blockposition, gameprofile);
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
