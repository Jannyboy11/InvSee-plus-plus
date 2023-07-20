package com.janboerman.invsee.glowstone;

import static com.janboerman.invsee.glowstone.GlowstoneHacks.getPlayerDir;
import static com.janboerman.invsee.glowstone.GlowstoneHacks.readCompressed;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.resolve.UUIDResolveStrategy;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import com.janboerman.invsee.spigot.internal.LogRecord;
import com.janboerman.invsee.spigot.internal.PlayerFileHelper;
import net.glowstone.GlowServer;
import net.glowstone.io.nbt.NbtPlayerDataService;
import net.glowstone.util.nbt.CompoundTag;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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
        GlowServer server = (GlowServer) plugin.getServer();
        NbtPlayerDataService playerDataService = (NbtPlayerDataService) server.getPlayerDataService();

        File playerDirectory = getPlayerDir(playerDataService);
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
                    final UUID playerId = uuidFromFileName(fileName);
                    final Executor syncExecutor = playerId == null ? scheduler::executeSyncGlobal : runnable -> scheduler.executeSyncPlayer(playerId, runnable, null);

                    try {

                        CompletableFuture<CompoundTag> compoundFuture = CompletableFuture.completedFuture(readCompressed(playerFile));
                        // if reading the player file asynchronously fails, we retry on the main thread.
                        compoundFuture = compoundFuture.handleAsync((tag, asyncEx) -> {
                            if (asyncEx == null) {
                                return tag;
                            } else {
                                try {
                                    return readCompressed(playerFile);
                                } catch (IOException syncEx) {
                                    //too bad, could not read this player save file synchronously.
                                    syncEx.addSuppressed(asyncEx);
                                    throw new CompletionException(syncEx);
                                }
                            }
                        }, syncExecutor);

                        try {
                            CompoundTag compound = compoundFuture.get(); // we join the (possibly synchronous!) future back into our async future!
                            if (tagHasLastKnownName(compound, userName)) {
                                String uuid = fileName.substring(0, 36);
                                try {
                                    //finally, we got there!
                                    return Optional.of(UUID.fromString(uuid));
                                } catch (IllegalArgumentException e) {
                                    //log exception only later in case the *correct* player file couldn't be found.
                                    errors.add(new LogRecord(Level.WARNING, "Encountered player save file name that is not a uuid: " + fileName, e));
                                }
                            }
                        } catch (ExecutionException e) {
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

    private static UUID uuidFromFileName(String fileName) {
        String uuidChars = fileName.substring(0, 36);
        try {
            return UUID.fromString(uuidChars);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean tagHasLastKnownName(CompoundTag tag, String userName) {
        if (tag.isCompound("bukkit")) {
            CompoundTag bukkit = tag.getCompound("bukkit");
            if (bukkit.isString("lastKnownName")) {
                String lastKnownName = bukkit.getString("lastKnownName");
                return lastKnownName.equals(userName);
            }
        }
        return false;
    }

}
