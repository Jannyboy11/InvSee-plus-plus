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


    // ===== Magma =====
    // https://github.com/Jannyboy11/InvSee-plus-plus/issues/43#issuecomment-1493377971

    public boolean func_184812_l_() {
        return isCreative();
    }

    public boolean func_175149_v() {
        return isSpectator();
    }

}
