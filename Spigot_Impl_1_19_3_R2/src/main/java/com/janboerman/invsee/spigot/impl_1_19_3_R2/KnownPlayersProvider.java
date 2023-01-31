package com.janboerman.invsee.spigot.impl_1_19_3_R2;

import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import static com.janboerman.invsee.spigot.impl_1_19_3_R2.HybridServerSupport.getPlayerDir;
import com.janboerman.invsee.utils.StringHelper;
import static com.janboerman.invsee.spigot.internal.NBTConstants.*;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;

public class KnownPlayersProvider implements OfflinePlayerProvider {

	private final Plugin plugin;

	public KnownPlayersProvider(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void getAll(final Consumer<String> result) {
		CraftServer craftServer = (CraftServer) plugin.getServer();
		PlayerDataStorage worldNBTStorage = craftServer.getHandle().playerIo;

		File playerDirectory = getPlayerDir(worldNBTStorage);
		if (!playerDirectory.exists() || !playerDirectory.isDirectory())
			return;

		File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> fileName.endsWith(".dat"));
		assert playerFiles != null : "playerFiles is not a directory?";
		for (File playerFile : playerFiles) {
			try {
				readName(result, playerFile);
			} catch (IOException | ReportedException e1) {
				//did not work, try again on main thread:
				plugin.getServer().getScheduler().runTask(plugin, () -> {
					try {
						readName(result, playerFile);
					} catch (IOException | ReportedException e2) {
						e2.addSuppressed(e1);
						plugin.getLogger().log(Level.WARNING, "Error reading player's save file " + playerFile.getAbsolutePath(), e2);
					}
				});
			}
		}
	}

	@Override
	public void getWithPrefix(final String prefix, final Consumer<String> result) {
		CraftServer craftServer = (CraftServer) plugin.getServer();
		PlayerDataStorage worldNBTStorage = craftServer.getHandle().playerIo;

		File playerDirectory = getPlayerDir(worldNBTStorage);
		if (!playerDirectory.exists() || !playerDirectory.isDirectory())
			return;

		File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> fileName.endsWith(".dat"));
		assert playerFiles != null : "playerFiles is not a directory?";
		for (File playerFile : playerFiles) {
			try {
				readName(prefix, result, playerFile);
			} catch (IOException | ReportedException e1) {
				//did not work, try again on main thread:
				plugin.getServer().getScheduler().runTask(plugin, () -> {
					try {
						readName(prefix, result, playerFile);
					} catch (IOException | ReportedException e2) {
						e2.addSuppressed(e1);
						plugin.getLogger().log(Level.WARNING, "Error reading player's save file " + playerFile.getAbsolutePath(), e2);
					}
				});
			}
		}
	}

	private static void readName(Consumer<String> reader, File playerFile) throws IOException, ReportedException {
		String name = readName(playerFile);
		if (name != null) {
			reader.accept(name);
		}
	}

	private static void readName(String prefix, Consumer<String> reader, File playerFile) throws IOException, ReportedException {
		String name = readName(playerFile);
		if (name != null && StringHelper.startsWithIgnoreCase(name, prefix)) {
			reader.accept(name);
		}
	}

	private static String readName(File playerFile) throws IOException, ReportedException {
		CompoundTag compound = NbtIo.readCompressed(playerFile);
		if (compound.contains("bukkit", TAG_COMPOUND)) {
			CompoundTag bukkit = compound.getCompound("bukkit");
			if (bukkit.contains("lastKnownName", TAG_STRING)) {
				return bukkit.getString("lastKnownName");
			}
		}
		return null;
	}

}

