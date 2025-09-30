package com.janboerman.invsee.spigot.impl_1_21_9_R6;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.resolve.UUIDResolveStrategy;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import com.janboerman.invsee.spigot.internal.LogRecord;
import com.janboerman.invsee.spigot.internal.PlayerFileHelper;

import org.bukkit.craftbukkit.v1_21_R6.CraftServer;
import org.bukkit.plugin.Plugin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class UUIDSearchSaveFilesStrategy implements UUIDResolveStrategy {

	private final Plugin plugin;
	private final Scheduler scheduler;

	public UUIDSearchSaveFilesStrategy(Plugin plugin, Scheduler scheduler) {
		this.plugin = plugin;
		this.scheduler = scheduler;
	}

	@Override
	public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
		CraftServer craftServer = (CraftServer) plugin.getServer();
		PlayerDataStorage worldNBTStorage = craftServer.getHandle().playerIo;

		File playerDirectory = HybridServerSupport.getPlayerDir(worldNBTStorage);
		if (!playerDirectory.exists() || !playerDirectory.isDirectory())
			return CompletedEmpty.the();
		
		return CompletableFuture.supplyAsync(() -> {
			File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> PlayerFileHelper.isPlayerSaveFile(fileName));
			if (playerFiles != null) {
				List<LogRecord> errors = new CopyOnWriteArrayList<>();

				//search through the save files, find the save file which has the lastKnownName of the requested player.
				playerFilesLoop:
				for (File playerFile : playerFiles) {
					final String fileName = playerFile.getName();
					final Path filePath = playerFile.toPath();
					final UUID playerId = uuidFromFileName(fileName);
					final Executor syncExecutor = playerId == null ? scheduler::executeSyncGlobal : runnable -> scheduler.executeSyncPlayer(playerId, runnable, null);

					//I now finally understand the appeal of libraries like Cats Effect / ZIO.
					try {
						CompletableFuture<CompoundTag> compoundFuture = CompletableFuture.completedFuture(net.minecraft.nbt.NbtIo.readCompressed(filePath, NbtAccounter.unlimitedHeap()));
						// if reading the player file asynchronously fails, we retry on the main thread.
						compoundFuture = compoundFuture.exceptionallyAsync(asyncEx -> {
							try {
								return net.minecraft.nbt.NbtIo.readCompressed(filePath, NbtAccounter.unlimitedHeap());
							} catch (IOException syncEx) {
								//too bad, could not read this player save file synchronously.
								syncEx.addSuppressed(asyncEx);
								throw new CompletionException(syncEx);
							}
						}, syncExecutor);

						try {
							CompoundTag compound = compoundFuture.get(); // we join the (possibly synchronous!) future back into our async future!
							if (tagHasLastKnownName(compound, userName)) {
								String uuid = fileName.substring(0, fileName.length() - 4);
								if (uuid.startsWith("-")) uuid = uuid.substring(1);
								try {
									UUID uniqueId = UUID.fromString(uuid);
									return Optional.of(uniqueId);
								} catch (IllegalArgumentException e) {
									//log exception only later in case the *correct* player file couldn't be found.
									errors.add(new LogRecord(Level.WARNING, "Encountered player save file name that is not a uuid: " + fileName, e));
								}
							}
						} catch (ExecutionException e) {
							// could not 'join' the future. nothing useful we can do here - we need to let some other strategy resolve the UUID instead.
							Throwable syncEx = e.getCause();
							errors.add(new LogRecord(Level.SEVERE, "Encountered player save file containing invalid NBT: " + fileName, syncEx));
							continue playerFilesLoop;
						} catch (InterruptedException e) {
							// idem.
							continue playerFilesLoop;
						}
					} catch (IOException e) {
						plugin.getLogger().log(Level.WARNING, "Error reading player's save file: " + playerFile.getAbsolutePath(), e);
					}
				}

				//log warnings only if the *correct* player file was missing.
				for (LogRecord error : errors) {
					plugin.getLogger().log(error.level, error.message, error.cause);
				}
			}
			return Optional.empty();
		}, scheduler::executeAsync);
	}

	private static boolean tagHasLastKnownName(CompoundTag compound, String userName) {
		return compound.getCompound("bukkit")
				.flatMap(bukkitCompound -> bukkitCompound.getString("lastKnownName"))
				.filter(lastKnownName -> lastKnownName.equals(userName))
				.isPresent();
	}

	private static UUID uuidFromFileName(String fileName) {
		if (fileName == null || fileName.length() < 36) return null;
		String uuidChars = fileName.substring(0, 36);
		try {
			return UUID.fromString(uuidChars);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
