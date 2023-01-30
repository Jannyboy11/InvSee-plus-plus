package com.janboerman.invsee.spigot.impl_1_18_2_R2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.plugin.Plugin;

import com.janboerman.invsee.spigot.api.resolve.UUIDResolveStrategy;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import com.janboerman.invsee.spigot.internal.LogRecord;
import static com.janboerman.invsee.spigot.internal.NBTConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

public class UUIDSearchSaveFilesStrategy implements UUIDResolveStrategy {

	private final Plugin plugin;

	public UUIDSearchSaveFilesStrategy(Plugin plugin) {
		this.plugin = plugin;
	}

	private Executor serverThreadExecutor() {
		return runnable -> {
			if (plugin.getServer().isPrimaryThread()) { runnable.run(); }
			else { plugin.getServer().getScheduler().runTask(plugin, runnable); }
		};
	}

	private Executor asyncExecutor() {
		return runnable -> {
			if (plugin.getServer().isPrimaryThread()) { plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable); }
			else runnable.run();
		};
	}

	@Override
	public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
		CraftServer craftServer = (CraftServer) plugin.getServer();
		PlayerDataStorage worldNBTStorage = craftServer.getHandle().playerIo;

		File playerDirectory = PlayerDirectory.getPlayerDir(worldNBTStorage);
		if (!playerDirectory.exists() || !playerDirectory.isDirectory())
			return CompletedEmpty.the();

		return CompletableFuture.supplyAsync(() -> {
			File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> fileName.endsWith(".dat"));
			if (playerFiles != null) {
				List<LogRecord> errors = new CopyOnWriteArrayList<>();

				//search through the save files, find the save file which has the lastKnownName of the quested player.
				playerFilesLoop:
				for (File playerFile : playerFiles) {
					final String fileName = playerFile.getName();

					//I now finally understand the appeal of libraries like Cats Effect / ZIO.
					try {
						CompletableFuture<CompoundTag> compoundFuture = CompletableFuture.completedFuture(net.minecraft.nbt.NbtIo.readCompressed(playerFile));
						// if reading the player file asynchronously fails, we retry on the main thread.
						compoundFuture = compoundFuture.exceptionallyAsync(asyncEx -> {
							try {
								return net.minecraft.nbt.NbtIo.readCompressed(playerFile);
							} catch (IOException syncEx) {
								//too bad, could not read this player save file synchronously.
								syncEx.addSuppressed(asyncEx);
								throw new CompletionException(syncEx);
							}
						}, serverThreadExecutor());

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
		}, asyncExecutor());
	}

	private static final boolean tagHasLastKnownName(CompoundTag compound, String userName) {
		if (compound.contains("bukkit", TAG_COMPOUND)) {
			CompoundTag bukkit = compound.getCompound("bukkit");
			if (bukkit.contains("lastKnownName", TAG_STRING)) {
				String lastKnownName = bukkit.getString("lastKnownName");
				return lastKnownName.equalsIgnoreCase(userName);
			}
		}

		return false;
	}

}
