package com.janboerman.invsee.glowstone;

import static com.janboerman.invsee.glowstone.GlowstoneHacks.getPlayerDir;
import static com.janboerman.invsee.glowstone.GlowstoneHacks.readCompressed;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.resolve.NameResolveStrategy;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import com.janboerman.invsee.utils.Rethrow;
import net.glowstone.GlowServer;
import net.glowstone.io.nbt.NbtPlayerDataService;
import net.glowstone.util.nbt.CompoundTag;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
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
        GlowServer server = (GlowServer) plugin.getServer();
        NbtPlayerDataService playerDataService = (NbtPlayerDataService) server.getPlayerDataService();

        File playerDirectory = getPlayerDir(playerDataService);
        if (!playerDirectory.exists() || !playerDirectory.isDirectory())
            return CompletedEmpty.the();

        File playerFile = new File(playerDirectory, uniqueId.toString() + ".dat");
        if (!playerFile.exists() || playerFile.isDirectory())
            return CompletedEmpty.the();

        return CompletableFuture.supplyAsync(() -> {
            try {
                CompoundTag compound = readCompressed(playerFile);
                if (compound.isCompound("bukkit")) {
                    CompoundTag bukkit = compound.getCompound("bukkit");
                    if (bukkit.isString("lastKnownName")) {
                        return Optional.of(bukkit.getString("lastKnownName"));
                    }
                }
                return Optional.empty();
            } catch (IOException e) {
                return Rethrow.unchecked(e);
            }
        }, scheduler::executeAsync);
    }

}
