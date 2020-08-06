package com.janboerman.invsee.spigot.impl_1_16;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InvseeImpl extends InvseeAPI {

    static CompletableFuture COMPLETED_EMPTY = InvseeAPI.COMPLETED_EMPTY;
    static ItemStack EMPTY_STACK = ItemStack.b;

    public InvseeImpl(Plugin plugin) {
        super(plugin);
        uuidResolveStrategies.add(1, new SearchSaveFilesStrategy(plugin));
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        UUID uuid = player.getUniqueId();
        CraftInventoryPlayer craftInventory = (CraftInventoryPlayer) player.getInventory();
        PlayerInventory nmsInventory = craftInventory.getInventory();
        MainNmsInventory spectatorInv = new MainNmsInventory(uuid, nmsInventory.items, nmsInventory.armor, nmsInventory.extraSlots, title);
        MainBukkitInventory bukkitInventory = new MainBukkitInventory(spectatorInv);
        spectatorInv.bukkit = bukkitInventory;
        return bukkitInventory;
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        UUID uuid = player.getUniqueId();
        CraftInventory craftInventory = (CraftInventory) player.getEnderChest();
        InventoryEnderChest nmsInventory = (InventoryEnderChest) craftInventory.getInventory();
        EnderNmsInventory spectatorInv = new EnderNmsInventory(uuid, nmsInventory.items, title);
        EnderBukkitInventory bukkitInventory = new EnderBukkitInventory(spectatorInv);
        spectatorInv.bukkit = bukkitInventory;
        return bukkitInventory;
    }

    @Override
    protected CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID player, String title) {

        CraftServer server = (CraftServer) plugin.getServer();
        DedicatedPlayerList playerList = server.getHandle();
        WorldNBTStorage worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        GameProfile gameProfile = new GameProfile(player, "InvSee++"); //only UUID is important.

        FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
                world.getHandle(),
                new BlockPosition(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()),
                gameProfile);

        return CompletableFuture.supplyAsync(() -> {
            NBTTagCompound playerCompound = worldNBTStorage.load(fakeEntityHuman);
            if (playerCompound != null) {
                fakeEntityHuman.loadData(playerCompound);   //only player-specific stuff
            } //else: no player save file exists.

            CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
            return Optional.of(spectateInventory(craftHumanEntity, title));

        }, runnable -> server.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    @Override
    protected CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID player, String title) {
        //TODO un-duplicate?
        CraftServer server = (CraftServer) plugin.getServer();
        DedicatedPlayerList playerList = server.getHandle();
        WorldNBTStorage worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        GameProfile gameProfile = new GameProfile(player, "InvSee++"); //only UUID is important.

        FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
                world.getHandle(),
                new BlockPosition(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()),
                gameProfile);

        return CompletableFuture.supplyAsync(() -> {
            NBTTagCompound playerCompound = worldNBTStorage.load(fakeEntityHuman);
            if (playerCompound != null) {
                fakeEntityHuman.loadData(playerCompound);   //only player-specific stuff
            } //else: no player save file exists.

            CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
            return Optional.of(spectateEnderChest(craftHumanEntity, title));

        }, runnable -> server.getScheduler().runTaskAsynchronously(plugin, runnable));
    }


    @Override
    protected CompletableFuture<Void> saveInventory(MainSpectatorInventory newInventory) {

        CraftServer server = (CraftServer) plugin.getServer();
        DedicatedPlayerList playerList = server.getHandle();
        WorldNBTStorage worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        GameProfile gameProfile = new GameProfile(newInventory.getSpectatedPlayer(), "InvSee++"); //only UUID is important.

        FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
                world.getHandle(),
                new BlockPosition(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()),
                gameProfile);

        return CompletableFuture.runAsync(() -> {
            NBTTagCompound playerCompound = worldNBTStorage.load(fakeEntityHuman);
            if (playerCompound != null) {
                fakeEntityHuman.load(playerCompound);   //all entity stuff + player stuff
            } //else: no player save file exists

            CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
            MainSpectatorInventory currentInventory = spectateInventory(craftHumanEntity, newInventory.getTitle());
            currentInventory.setStorageContents(newInventory.getStorageContents());
            currentInventory.setArmourContents(newInventory.getArmourContents());
            currentInventory.setOffHandContents(newInventory.getOffHandContents());

            worldNBTStorage.save(fakeEntityHuman);

        }, runnable -> server.getScheduler().runTaskAsynchronously(plugin, runnable));

    }

    @Override
    protected CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory newInventory) {
        //TODO un-duplicate?
        CraftServer server = (CraftServer) plugin.getServer();
        DedicatedPlayerList playerList = server.getHandle();
        WorldNBTStorage worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        GameProfile gameProfile = new GameProfile(newInventory.getSpectatedPlayer(), "InvSee++"); //only UUID is important.

        FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
                world.getHandle(),
                new BlockPosition(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()),
                gameProfile);

        return CompletableFuture.runAsync(() -> {
            NBTTagCompound playerCompound = worldNBTStorage.load(fakeEntityHuman);
            if (playerCompound != null) {
                fakeEntityHuman.load(playerCompound);   //all entity stuff + player stuff
            } //else: no player save file exists

            CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
            EnderSpectatorInventory currentInventory = spectateEnderChest(craftHumanEntity, newInventory.getTitle());
            currentInventory.setStorageContents(newInventory.getStorageContents());

            worldNBTStorage.save(fakeEntityHuman);

        }, runnable -> server.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

}
