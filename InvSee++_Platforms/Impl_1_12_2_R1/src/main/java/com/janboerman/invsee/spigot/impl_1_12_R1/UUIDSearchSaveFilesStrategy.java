package com.janboerman.invsee.spigot.impl_1_12_R1;

import com.janboerman.invsee.spigot.api.resolve.UUIDResolveStrategy;
import static com.janboerman.invsee.spigot.impl_1_12_R1.HybridServerSupport.getPlayerDir;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import com.janboerman.invsee.spigot.internal.LogRecord;
import static com.janboerman.invsee.spigot.internal.NBTConstants.*;

import com.janboerman.invsee.spigot.api.Scheduler;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.WorldNBTStorage;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

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
        WorldNBTStorage worldNBTStorage = (WorldNBTStorage) craftServer.getHandle().playerFileData;

        File playerDirectory = getPlayerDir(worldNBTStorage);
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
                    final UUID playerId = uuidFromFileName(fileName);
                    final Executor syncExecutor = playerId == null ? scheduler::executeSyncGlobal : runnable -> scheduler.executeSyncPlayer(playerId, runnable, null);

                    //I now finally understand the appeal of libraries like Cats Effect / ZIO.
                    try {
                        CompletableFuture<NBTTagCompound> compoundFuture = CompletableFuture.completedFuture(NBTCompressedStreamTools.a(new FileInputStream(playerFile)));
                        // if reading the player file asynchronously fails, we retry on the main thread.
                        compoundFuture = compoundFuture.handleAsync((tag, asyncEx) -> {
                            if (asyncEx == null) {
                                return tag;
                            } else {
                                try {
                                    return NBTCompressedStreamTools.a(new FileInputStream(playerFile));
                                } catch (IOException syncEx) {
                                    //too bad, could not read this player save file synchronously.
                                    syncEx.addSuppressed(asyncEx);
                                    throw new CompletionException(syncEx);
                                }
                            }
                        }, syncExecutor);

                        try {
                            NBTTagCompound compound = compoundFuture.get(); // we join the (possibly synchronous!) future back into our async future!
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

    private static final boolean tagHasLastKnownName(NBTTagCompound compound, String userName) {
        if (compound.hasKeyOfType("bukkit", TAG_COMPOUND)) {
            NBTTagCompound bukkit = compound.getCompound("bukkit");
            if (bukkit.hasKeyOfType("lastKnownName", TAG_STRING)) {
                String lastKnownName = bukkit.getString("lastKnownName");
                return lastKnownName.equalsIgnoreCase(userName);
            }
        }

        return false;
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
