package com.janboerman.invsee.spigot.impl_1_16_R1;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class InvseeImpl extends InvseeAPI {

    static CompletableFuture COMPLETED_EMPTY = InvseeAPI.COMPLETED_EMPTY;
    static ItemStack EMPTY_STACK = ItemStack.b;

    public InvseeImpl(Plugin plugin) {
        super(plugin);
        uuidResolveStrategies.add(1, new SearchSaveFilesStrategy(plugin));
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        MainNmsInventory spectatorInv = new MainNmsInventory(((CraftHumanEntity) player).getHandle(), title);
        MainBukkitInventory bukkitInventory = new MainBukkitInventory(spectatorInv);
        spectatorInv.bukkit = bukkitInventory;

        InventoryView targetView = player.getOpenInventory();
        bukkitInventory.watch(targetView);

        return bukkitInventory;
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        CraftInventory craftInventory = (CraftInventory) player.getEnderChest();
        InventoryEnderChest nmsInventory = (InventoryEnderChest) craftInventory.getInventory();
        EnderNmsInventory spectatorInv = new EnderNmsInventory(uuid, name, nmsInventory.items, title);
        EnderBukkitInventory bukkitInventory = new EnderBukkitInventory(spectatorInv);
        spectatorInv.bukkit = bukkitInventory;
        return bukkitInventory;
    }

    @Override
    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID player, String name, String title) {
        return createOffline(player, name, title, this::spectateInventory);
    }

    @Override
    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID player, String name, String title) {
        return createOffline(player, name, title, this::spectateEnderChest);
    }

    @Override
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory newInventory) {
        return save(newInventory, this::spectateInventory, (currentInv, newInv) -> {
            currentInv.setStorageContents(newInv.getStorageContents());
            currentInv.setArmourContents(newInv.getArmourContents());
            currentInv.setOffHandContents(newInv.getOffHandContents());
            currentInv.setCursorContents(newInv.getCursorContents());
            currentInv.setPersonalContents(newInv.getPersonalContents());
        });
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory newInventory) {
        return save(newInventory, this::spectateEnderChest, (currentInv, newInv) -> {
            currentInv.setStorageContents(newInv.getStorageContents());
        });
    }

    private <IS extends SpectatorInventory> CompletableFuture<Optional<IS>> createOffline(UUID player, String name, String title, BiFunction<? super HumanEntity, String, IS> invCreator) {
        CraftServer server = (CraftServer) plugin.getServer();
        DedicatedPlayerList playerList = server.getHandle();
        WorldNBTStorage worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        GameProfile gameProfile = new GameProfile(player, name);

        FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
                world.getHandle(),
                new BlockPosition(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()),
                gameProfile);

        return CompletableFuture.supplyAsync(() -> {
            NBTTagCompound playerCompound = worldNBTStorage.load(fakeEntityHuman);
            if (playerCompound != null) {
                fakeEntityHuman.loadData(playerCompound);   //only player-specific stuff
            } //else: player save file exists.

            CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
            return Optional.of(invCreator.apply(craftHumanEntity, title));

        }, asyncExecutor);
    }

    private <SI extends SpectatorInventory> CompletableFuture<Void> save(SI newInventory, BiFunction<? super HumanEntity, String, SI> currentInvProvider, BiConsumer<SI, SI> transfer) {

        CraftServer server = (CraftServer) plugin.getServer();
        DedicatedPlayerList playerList = server.getHandle();
        WorldNBTStorage worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        GameProfile gameProfile = new GameProfile(newInventory.getSpectatedPlayerId(), newInventory.getSpectatedPlayerName());

        EntityPlayer fakeEntityPlayer = new EntityPlayer(
                server.getServer(),
                world.getHandle(),
                gameProfile,
                new PlayerInteractManager(world.getHandle()));

        return CompletableFuture.runAsync(() -> {
            NBTTagCompound playerCompound = worldNBTStorage.load(fakeEntityPlayer);
            if (playerCompound != null) {
                fakeEntityPlayer.load(playerCompound);   //all entity stuff + player stuff
            } //else: no player save file exists

            CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityPlayer);
            SI currentInv = currentInvProvider.apply(craftHumanEntity, newInventory.getTitle());

            transfer.accept(currentInv, newInventory);

            worldNBTStorage.save(fakeEntityPlayer);
        }, asyncExecutor);
    }

}
