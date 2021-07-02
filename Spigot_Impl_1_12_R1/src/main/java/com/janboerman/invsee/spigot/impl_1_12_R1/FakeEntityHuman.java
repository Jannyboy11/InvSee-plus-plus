package com.janboerman.invsee.spigot.impl_1_12_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.World;

class FakeEntityHuman extends EntityHuman {

    FakeEntityHuman(World world, GameProfile gameprofile) {
        super(world, gameprofile);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean z() { //isCreative
        return false;
    }

}