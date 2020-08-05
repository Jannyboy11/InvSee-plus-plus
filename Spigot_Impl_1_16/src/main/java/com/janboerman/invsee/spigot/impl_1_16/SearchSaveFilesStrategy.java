package com.janboerman.invsee.spigot.impl_1_16;

import com.janboerman.invsee.spigot.api.UUIDResolveStrategy;
import net.minecraft.server.v1_16_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import net.minecraft.server.v1_16_R1.WorldNBTStorage;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SearchSaveFilesStrategy implements UUIDResolveStrategy {

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

    public SearchSaveFilesStrategy(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUUID(String userName) {
        CraftServer craftServer = (CraftServer) plugin.getServer();
        WorldNBTStorage worldNBTStorage = craftServer.getHandle().playerFileData;

        File playerDirectory = worldNBTStorage.getPlayerDir();
        if (!playerDirectory.exists() || !playerDirectory.isDirectory())
            return InvseeImpl.COMPLETED_EMPTY;

        return CompletableFuture.supplyAsync(() -> {
            File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> fileName.endsWith(".dat"));
            for (File playerFile : playerFiles) {
                try {
                    NBTTagCompound compound = NBTCompressedStreamTools.a(new FileInputStream(playerFile));
                    if (compound.hasKeyOfType("bukkit", TAG_COMPOUND)) {
                        NBTTagCompound bukkit = compound.getCompound("bukkit");
                        if (bukkit.hasKeyOfType("lastKnownName", TAG_STRING)) {
                            String lastKnownName = bukkit.getString("lastKnownName");
                            if (lastKnownName.equalsIgnoreCase(userName)) {
                                String fileName = playerFile.getName();
                                String uuid = fileName.substring(0, fileName.length() - 4);
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
