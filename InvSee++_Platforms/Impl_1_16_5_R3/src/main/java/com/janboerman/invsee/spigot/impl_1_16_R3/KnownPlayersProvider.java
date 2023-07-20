package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.internal.PlayerFileHelper;
import com.janboerman.invsee.utils.StringHelper;
import static com.janboerman.invsee.spigot.internal.NBTConstants.*;

import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.ReportedException;
import net.minecraft.server.v1_16_R3.WorldNBTStorage;

import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Level;

public class KnownPlayersProvider implements OfflinePlayerProvider {

    private final Plugin plugin;
    private final Scheduler scheduler;

    public KnownPlayersProvider(Plugin plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    @Override
    public void getAll(final Consumer<String> result) {
        CraftServer craftServer = (CraftServer) plugin.getServer();
        WorldNBTStorage worldNBTStorage = craftServer.getHandle().playerFileData;

        File playerDirectory = HybridServerSupport.getPlayerDir(worldNBTStorage);
        if (!playerDirectory.exists() || !playerDirectory.isDirectory())
            return;

        File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> PlayerFileHelper.isPlayerSaveFile(fileName));
        for (File playerFile : playerFiles) {
            try {
                readName(result, playerFile);
            } catch (IOException | ReportedException e1) {
                //did not work, try again on main thread:
                UUID playerId = uuidFromFileName(playerFile.getName());
                Executor executor = playerId == null ? scheduler::executeSyncGlobal : runnable -> scheduler.executeSyncPlayer(playerId, runnable, null);

                executor.execute(() -> {
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
        WorldNBTStorage worldNBTStorage = (WorldNBTStorage) craftServer.getHandle().playerFileData;

        File playerDirectory = HybridServerSupport.getPlayerDir(worldNBTStorage);
        if (!playerDirectory.exists() || !playerDirectory.isDirectory())
            return;

        File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> PlayerFileHelper.isPlayerSaveFile(fileName));
        for (File playerFile : playerFiles) {
            try {
                readName(prefix, result, playerFile);
            } catch (IOException | ReportedException e1) {
                //did not work, try again on main thread:
                UUID playerId = uuidFromFileName(playerFile.getName());
                Executor executor = playerId == null ? scheduler::executeSyncGlobal : runnable -> scheduler.executeSyncPlayer(playerId, runnable, null);

                executor.execute(() -> {
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

    private static UUID uuidFromFileName(String fileName) {
        if (fileName == null | fileName.length() < 36) return null;
        try {
            return UUID.fromString(fileName.substring(0, 36));
        } catch (IllegalArgumentException e) {
            return null;
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
        NBTTagCompound compound = NBTCompressedStreamTools.a(new FileInputStream(playerFile));
        if (compound.hasKeyOfType("bukkit", TAG_COMPOUND)) {
            NBTTagCompound bukkit = compound.getCompound("bukkit");
            if (bukkit.hasKeyOfType("lastKnownName", TAG_STRING)) {
                return bukkit.getString("lastKnownName");
            }
        }
        return null;
    }

}
