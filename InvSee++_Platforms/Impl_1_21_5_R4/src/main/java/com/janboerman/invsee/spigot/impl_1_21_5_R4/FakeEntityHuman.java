package com.janboerman.invsee.spigot.impl_1_21_5_R4;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

class FakeEntityHuman extends Player {

	FakeEntityHuman(Level world, BlockPos blockposition, float yaw, GameProfile gameprofile) {
		super(world, blockposition, yaw, gameprofile);
	}

	@Nullable
	@Override
	public GameType gameMode() {
		return null;
	}

	@Override
	public boolean isCreative() {
		return false;
	}

	@Override
	public boolean isSpectator() {
		return false;
	}

}
