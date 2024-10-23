package com.janboerman.invsee.spigot.impl_1_21_3_R2;

import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.resolve.NameResolveStrategy;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import static com.janboerman.invsee.spigot.internal.NBTConstants.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.v1_21_R2.CraftServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NameSearchSaveFilesStrategy implements NameResolveStrategy {

	private final Plugin plugin;
	private final Scheduler scheduler;

	public NameSearchSaveFilesStrategy(Plugin plugin, Scheduler scheduler) {
		this.plugin = plugin;
		this.scheduler = scheduler;
	}

	@Override
	public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
		CraftServer craftServer = (CraftServer) plugin.getServer();
		PlayerDataStorage worldNBTStorage = craftServer.getHandle().playerIo;

		File playerDirectory = HybridServerSupport.getPlayerDir(worldNBTStorage);
		if (!playerDirectory.exists() || !playerDirectory.isDirectory())
			return CompletedEmpty.the();
		
		return CompletableFuture.supplyAsync(() -> {
			var optional = worldNBTStorage.load("InvSee++ Player", uniqueId.toString());
			return optional.map(compound -> {
				if (compound.contains("bukkit", TAG_COMPOUND)) {
					CompoundTag bukkit = compound.getCompound("bukkit");
					if (bukkit.contains("lastKnownName", TAG_STRING)) {
						return bukkit.getString("lastKnownName");
					}
				}
				return null;
			});
		}, scheduler::executeAsync);
	}

}
