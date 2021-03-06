package com.janboerman.invsee.spigot.impl_1_17_1_R1;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.plugin.Plugin;

import com.janboerman.invsee.spigot.api.UUIDResolveStrategy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class UUIDSearchSaveFilesStrategy implements UUIDResolveStrategy {

    private static final int TAG_END = 0;
    private static final int TAG_BYTE = 1;
    private static final int TAG_SHORT = 2;
    private static final int TAG_INT = 3;
    private static final int TAG_LONG = 4;
    private static final int TAG_FLOAT = 5;
    private static final int TAG_DOUBLE = 6;
    private static final int TAG_BYTE_ARRAY = 7;
    private static final int TAG_STRING = 8;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;
    private static final int TAG_INT_ARRAY = 11;
    private static final int TAG_LONG_ARRAY = 12;
    private static final int TAG_UNKNOWN = 99;

    private final Plugin plugin;

    public UUIDSearchSaveFilesStrategy(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
        CraftServer craftServer = (CraftServer) plugin.getServer();
        PlayerDataStorage worldNBTStorage = craftServer.getHandle().playerIo;

        File playerDirectory = worldNBTStorage.getPlayerDir();
        if (!playerDirectory.exists() || !playerDirectory.isDirectory())
            return InvseeImpl.COMPLETED_EMPTY;

        return CompletableFuture.supplyAsync(() -> {
            File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> fileName.endsWith(".dat"));
            for (File playerFile : playerFiles) {
                try {
                    CompoundTag compound = net.minecraft.nbt.NbtIo.readCompressed(playerFile);
                    if (compound.contains("bukkit", TAG_COMPOUND)) {
                        CompoundTag bukkit = compound.getCompound("bukkit");
                        if (bukkit.contains("lastKnownName", TAG_STRING)) {
                            String lastKnownName = bukkit.getString("lastKnownName");
                            if (lastKnownName.equalsIgnoreCase(userName)) {
                                String fileName = playerFile.getName();
                                String uuid = fileName.substring(0, fileName.length()- 4);
                                if (uuid.startsWith("-")) uuid = uuid.substring(1);
                                try {
                                    UUID uniqueId = UUID.fromString(uuid);
                                    return Optional.of(uniqueId);
                                } catch (IllegalArgumentException e) {
                                    plugin.getLogger().log(Level.WARNING, "Encountered player save file name that is not a uuid: " + fileName, e);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Error reading player's save file: " + playerFile.getAbsolutePath(), e);
                }
            }
            return Optional.empty();
        }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

}
