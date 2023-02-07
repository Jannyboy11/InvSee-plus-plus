package com.janboerman.invsee.spigot.impl_1_8_R3;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.World;

class FakeEntityHuman extends EntityHuman {

    FakeEntityHuman(World world, GameProfile gameprofile) {
        super(world, gameprofile);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

}