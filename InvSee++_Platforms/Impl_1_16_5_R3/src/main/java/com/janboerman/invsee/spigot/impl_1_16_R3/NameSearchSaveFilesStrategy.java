package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.resolve.NameResolveStrategy;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import static com.janboerman.invsee.spigot.internal.NBTConstants.*;
import com.janboerman.invsee.spigot.internal.Scheduler;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.WorldNBTStorage;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NameSearchSaveFilesStrategy implements NameResolveStrategy {

    private final Plugin plugin;
    private final Scheduler scheduler;

    public NameSearchSaveFilesStrategy(Plugin plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    @Override
    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
        CraftServer craftServer = (CraftServer) plugin.getServer();
        WorldNBTStorage worldNBTStorage = craftServer.getHandle().playerFileData;

        File playerDirectory = HybridServerSupport.getPlayerDir(worldNBTStorage);
        if (!playerDirectory.exists() || !playerDirectory.isDirectory())
            return CompletedEmpty.the();

        return CompletableFuture.supplyAsync(() -> {
            NBTTagCompound compound = worldNBTStorage.getPlayerData(uniqueId.toString());
            if (compound.hasKeyOfType("bukkit", TAG_COMPOUND)) {
                NBTTagCompound bukkit = compound.getCompound("bukkit");
                if (bukkit.hasKeyOfType("lastKnownName", TAG_STRING)) {
                    String lastKnownName = bukkit.getString("lastKnownName");
                    return Optional.of(lastKnownName);
                }
            }
            return Optional.empty();
        }, scheduler::executeAsync);
    }

}

