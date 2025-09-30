package com.janboerman.invsee.spigot.impl_1_21_9_R6;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.resolve.NameResolveStrategy;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;

import org.bukkit.craftbukkit.v1_21_R6.CraftServer;
import org.bukkit.plugin.Plugin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.PlayerDataStorage;

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
					return HybridServerSupport.load(worldNBTStorage, "InvSee++ Player", uniqueId.toString(), ThrowingProblemReporter.INSTANCE, RegistryAccess.EMPTY)
							.flatMap(playerTag -> playerTag.child("bukkit"))
							.flatMap(bukkitTag -> bukkitTag.getString("lastKnownName"));
				},
				scheduler::executeAsync);
	}

}
