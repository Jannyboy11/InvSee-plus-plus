package com.janboerman.invsee.spigot.impl_1_20_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

class FakeEntityHuman extends Player {

	FakeEntityHuman(Level world, BlockPos blockposition, float yaw, GameProfile gameprofile) {
		super(world, blockposition, yaw, gameprofile);
	}

	@Override
	public boolean isCreative() {
		return false;
	}

	@Override
	public boolean isSpectator() {
		return false;
	}


	// === workarounds for Mohist ===

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
	}
}
