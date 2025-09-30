package com.janboerman.invsee.spigot.impl_1_21_9_R6;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;

class FakeEntityHuman extends Player {

	FakeEntityHuman(Level world, GameProfile gameprofile) {
		super(world, gameprofile);
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

	@Override
	public void readAdditionalSaveData(ValueInput nbtCompound) {
		super.readAdditionalSaveData(nbtCompound);
	}
}
