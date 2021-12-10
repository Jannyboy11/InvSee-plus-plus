package com.janboerman.invsee.spigot.impl_1_18_1_R1;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class InvseeImpl extends InvseeAPI {

    static final CompletableFuture/*java.util.Optional.empty()*/ COMPLETED_EMPTY = InvseeAPI.COMPLETED_EMPTY;
    static final ItemStack EMPTY_STACK = ItemStack.EMPTY;

    public InvseeImpl(Plugin plugin) {
        super(plugin);
        uuidResolveStrategies.add(1, new UUIDSearchSaveFilesStrategy(plugin));
        nameResolveStrategies.add(1, new NameSearchSaveFilesStrategy(plugin));
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
    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title) {
        return createOffline(playerId, playerName, title, this::spectateInventory);
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
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        CraftInventory craftInventory = (CraftInventory) player.getEnderChest();
        PlayerEnderChestContainer nmsInventory = (PlayerEnderChestContainer) craftInventory.getInventory();
        EnderNmsInventory spectatorInv = new EnderNmsInventory(uuid, name, nmsInventory.items, title);
        EnderBukkitInventory bukkitInventory = new EnderBukkitInventory(spectatorInv);
        spectatorInv.bukkit = bukkitInventory;
        return bukkitInventory;
    }

    @Override
    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID player, String name, String title) {
        return createOffline(player, name, title, this::spectateEnderChest);
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
    	PlayerDataStorage worldNBTStorage = playerList.playerIo;
    	
    	CraftWorld world = (CraftWorld) server.getWorlds().get(0);
    	Location spawn = world.getSpawnLocation();
    	float yaw = 0F;
    	GameProfile gameProfile = new GameProfile(player, name);
    	
    	FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
    			world.getHandle(),
    			new BlockPos(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()),
    			yaw,
    			gameProfile);
    	
    	return CompletableFuture.supplyAsync(() -> {
    		CompoundTag playerCompound = worldNBTStorage.load(fakeEntityHuman);
    		if (playerCompound != null) {
    			fakeEntityHuman.readAdditionalSaveData(playerCompound);		//only player-specific stuff
    		} //else: player save file exists.
    		
    		CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
    		return Optional.of(invCreator.apply(craftHumanEntity, title));
    	}, asyncExecutor);
    }
    
    private <SI extends SpectatorInventory> CompletableFuture<Void> save(SI newInventory, BiFunction<? super HumanEntity, String, SI> currentInvProvider, BiConsumer<SI, SI> transfer) {
    	CraftServer server = (CraftServer) plugin.getServer();
    	DedicatedPlayerList playerList = server.getHandle();
    	PlayerDataStorage worldNBTStorage = playerList.playerIo;
    	
    	CraftWorld world = (CraftWorld) server.getWorlds().get(0);
    	GameProfile gameProfile = new GameProfile(newInventory.getSpectatedPlayerId(), newInventory.getSpectatedPlayerName());
    	
    	ServerPlayer fakeEntityPlayer = new ServerPlayer(
    			server.getServer(),
    			world.getHandle(),
    			gameProfile);
    	
    	return CompletableFuture.runAsync(() -> {
    		CompoundTag playerCompound = worldNBTStorage.load(fakeEntityPlayer);
    		if (playerCompound != null) {
    			fakeEntityPlayer.load(playerCompound);		//all entity stuff + player stuff
    		} //else: no player save file exists
    		
    		CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityPlayer);
    		SI currentInv = currentInvProvider.apply(craftHumanEntity, newInventory.getTitle());
    		
    		transfer.accept(currentInv, newInventory);
    		
    		worldNBTStorage.save(fakeEntityPlayer);
    	}, asyncExecutor);
    }
}
