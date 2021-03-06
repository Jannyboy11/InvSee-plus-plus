package com.janboerman.invsee.spigot.impl_1_12_R1;

import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.utils.StringHelper;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.ReportedException;
import net.minecraft.server.v1_12_R1.WorldNBTStorage;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class KnownPlayersProvider implements OfflinePlayerProvider {

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

    public KnownPlayersProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Set<String> getAll() {
        CraftServer craftServer = (CraftServer) plugin.getServer();
        WorldNBTStorage worldNBTStorage = (WorldNBTStorage) craftServer.getHandle().playerFileData;

        File playerDirectory = worldNBTStorage.getPlayerDir();
        if (!playerDirectory.exists() || !playerDirectory.isDirectory())
            return Set.of();

        Set<String> result = new ConcurrentSkipListSet<>(String.CASE_INSENSITIVE_ORDER);

        File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> fileName.endsWith(".dat"));
        for (File playerFile : playerFiles) {
            try {
                NBTTagCompound compound = NBTCompressedStreamTools.a(new FileInputStream(playerFile));
                if (compound.hasKeyOfType("bukkit", TAG_COMPOUND)) {
                    NBTTagCompound bukkit = compound.getCompound("bukkit");
                    if (bukkit.hasKeyOfType("lastKnownName", TAG_STRING)) {
                        String lastKnownName = bukkit.getString("lastKnownName");
                        result.add(lastKnownName);
                    }
                }
            } catch (IOException | ReportedException e) {
                //plugin.getLogger().log(Level.WARNING, "Error reading player's save file " + playerFile.getAbsolutePath(), e);
            }
        }

        return result;
    }

    @Override
    public Set<String> getWithPrefix(String prefix) {
        CraftServer craftServer = (CraftServer) plugin.getServer();
        WorldNBTStorage worldNBTStorage = (WorldNBTStorage) craftServer.getHandle().playerFileData;

        File playerDirectory = worldNBTStorage.getPlayerDir();
        if (!playerDirectory.exists() || !playerDirectory.isDirectory())
            return Set.of();

        Set<String> result = new ConcurrentSkipListSet<>(String.CASE_INSENSITIVE_ORDER);

        File[] playerFiles = playerDirectory.listFiles((directory, fileName) -> fileName.endsWith(".dat"));
        for (File playerFile : playerFiles) {
            try {
                NBTTagCompound compound = NBTCompressedStreamTools.a(new FileInputStream(playerFile));
                if (compound.hasKeyOfType("bukkit", TAG_COMPOUND)) {
                    NBTTagCompound bukkit = compound.getCompound("bukkit");
                    if (bukkit.hasKeyOfType("lastKnownName", TAG_STRING)) {
                        String lastKnownName = bukkit.getString("lastKnownName");
                        if (StringHelper.startsWithIgnoreCase(lastKnownName, prefix)) {
                            result.add(lastKnownName);
                        }
                    }
                }
            } catch (IOException | ReportedException e) {
                //plugin.getLogger().log(Level.WARNING, "Error reading player's save file " + playerFile.getAbsolutePath(), e);
            }
        }

        return result;
    }

}
