package com.janboerman.invsee.glowstone;

import static com.janboerman.invsee.glowstone.GlowstoneHacks.getPlayerDir;
import static com.janboerman.invsee.glowstone.GlowstoneHacks.readCompressed;
import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.internal.PlayerFileHelper;
import com.janboerman.invsee.utils.StringHelper;
import net.glowstone.GlowServer;
import net.glowstone.io.nbt.NbtPlayerDataService;
import net.glowstone.util.nbt.CompoundTag;
import org.bukkit.plugin.Plugin;

import java.io.File;
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
    public void getAll(Consumer<String> result) {
        GlowServer server = (GlowServer) plugin.getServer();
        NbtPlayerDataService playerDataService = (NbtPlayerDataService) server.getPlayerDataService();

        File playerDirectory = getPlayerDir(playerDataService);
        if (!playerDirectory.exists() || !playerDirectory.exists())
            return;

        File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> PlayerFileHelper.isPlayerSaveFile(fileName));
        assert playerFiles != null : "playerFiles is not a directory?";
        for (File playerFile : playerFiles) {
            try {
                readName(result, playerFile);
            } catch (IOException e1) {
                //did not work, try again on main thread:
                UUID playerId = uuidFromFileName(playerFile.getName());
                Executor executor = playerId == null ? scheduler::executeSyncGlobal : runnable -> scheduler.executeSyncPlayer(playerId, runnable, null);

                executor.execute(() -> {
                    try {
                        readName(result, playerFile);
                    } catch (IOException e2) {
                        e2.addSuppressed(e1);
                        plugin.getLogger().log(Level.WARNING, "Error reading player's save file " + playerFile.getAbsolutePath(), e2);
                    }
                });
            }
        }
    }

    @Override
    public void getWithPrefix(String prefix, Consumer<String> result) {
        GlowServer server = (GlowServer) plugin.getServer();
        NbtPlayerDataService playerDataService = (NbtPlayerDataService) server.getPlayerDataService();

        File playerDirectory = getPlayerDir(playerDataService);
        if (!playerDirectory.exists() || !playerDirectory.exists())
            return;

        File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> PlayerFileHelper.isPlayerSaveFile(fileName));
        assert playerFiles != null : "playerFiles is not a directory?";
        for (File playerFile : playerFiles) {
            try {
                readName(prefix, result, playerFile);
            } catch (IOException e1) {
                //did not work, try again on main thread:
                UUID playerId = uuidFromFileName(playerFile.getName());
                Executor executor = playerId == null ? scheduler::executeSyncGlobal : runnable -> scheduler.executeSyncPlayer(playerId, runnable, null);

                executor.execute(() -> {
                    try {
                        readName(prefix, result, playerFile);
                    } catch (IOException e2) {
                        e2.addSuppressed(e1);
                        plugin.getLogger().log(Level.WARNING, "Error reading player's save file " + playerFile.getAbsolutePath(), e2);
                    }
                });
            }
        }
    }

    private static void readName(Consumer<String> reader, File playerFile) throws IOException {
        String name = readName(playerFile);
        if (name != null) {
            reader.accept(name);
        }
    }

    private static void readName(String prefix, Consumer<String> reader, File playerFile) throws IOException {
        String name = readName(playerFile);
        if (name != null && StringHelper.startsWithIgnoreCase(name, prefix)) {
            reader.accept(name);
        }
    }

    private static UUID uuidFromFileName(String fileName) {
        String uuidChars = fileName.substring(0, 36);
        try {
            return UUID.fromString(uuidChars);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String readName(File playerFile) throws IOException {
        CompoundTag root = readCompressed(playerFile);
        if (root.isCompound("bukkit")) {
            CompoundTag bukkit = root.getCompound("bukkit");
            if (bukkit.isString("lastKnownName")) {
                return bukkit.getString("lastKnownName");
            }
        }
        return null;
    }

}
