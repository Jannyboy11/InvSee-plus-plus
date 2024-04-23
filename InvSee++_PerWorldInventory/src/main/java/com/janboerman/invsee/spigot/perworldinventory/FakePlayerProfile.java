package com.janboerman.invsee.spigot.perworldinventory;

import static com.janboerman.invsee.utils.Compat.mapEntry;
import static com.janboerman.invsee.utils.Compat.mapOfEntries;

import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FakePlayerProfile implements PlayerProfile {

    private final FakePlayer player;
    private PlayerTextures textures;

    public FakePlayerProfile(FakePlayer fakePlayer) {
        this.player = fakePlayer;
    }

    @Nullable
    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Nullable
    @Override
    public String getName() {
        return player.getName();
    }

    @NotNull
    @Override
    public PlayerTextures getTextures() {
        return textures;
    }

    @Override
    public void setTextures(@Nullable PlayerTextures playerTextures) {
        this.textures = playerTextures;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @NotNull
    @Override
    public CompletableFuture<PlayerProfile> update() {
        return CompletableFuture.completedFuture(this);
    }

    @NotNull
    @Override
    public PlayerProfile clone() {
        return this;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return mapOfEntries(
                mapEntry("uuid", getUniqueId().toString()),
                mapEntry("name", getName()),
                mapEntry("textures", getTextures())
        );
    }
}
