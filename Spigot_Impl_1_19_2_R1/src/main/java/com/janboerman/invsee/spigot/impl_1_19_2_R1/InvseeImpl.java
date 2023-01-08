package com.janboerman.invsee.spigot.impl_1_19_2_R1;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.utils.TriFunction;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class InvseeImpl extends InvseeAPI {

    public InvseeImpl(Plugin plugin) {
        super(plugin);
        if (lookup.onlineMode(plugin.getServer())) {
            lookup.uuidResolveStrategies.add(new UUIDSearchSaveFilesStrategy(plugin));
        } else {
            // If we are in offline mode, then we should insert this strategy *before* the UUIDOfflineModeStrategy.
            lookup.uuidResolveStrategies.add(lookup.uuidResolveStrategies.size() - 1, new UUIDSearchSaveFilesStrategy(plugin));
        }
        lookup.nameResolveStrategies.add(2, new NameSearchSaveFilesStrategy(plugin));
    }

    @Override
    public void openMainSpectatorInventory(Player spectator, MainSpectatorInventory inv, String title, Mirror<PlayerInventorySlot> mirror) {
        CraftPlayer bukkitPlayer = (CraftPlayer) spectator;
        ServerPlayer nmsPlayer = bukkitPlayer.getHandle();
        MainBukkitInventory bukkitInventory = (MainBukkitInventory) inv;
        MainNmsInventory nmsInventory = bukkitInventory.getInventory();

        //this is what the nms does: nmsPlayer.openMenu(nmsWindow);
        //so let's emulate that!
        int windowId = nmsPlayer.nextContainerCounter();
        Inventory bottom = nmsPlayer.getInventory();
        MainNmsContainer nmsWindow = new MainNmsContainer(windowId, nmsInventory, bottom, nmsPlayer, mirror);
        nmsWindow.setTitle(CraftChatMessage.fromString(title != null ? title : inv.getTitle())[0]);
        boolean eventCancelled = CraftEventFactory.callInventoryOpenEvent(nmsPlayer, nmsWindow, false) == null; //closes current open inventory if one is already open
        if (!eventCancelled) {
            nmsPlayer.containerMenu = nmsWindow;
            nmsPlayer.connection.send(new ClientboundOpenScreenPacket(windowId, nmsWindow.getType(), nmsWindow.getTitle()));
            nmsPlayer.initMenu(nmsWindow);
        }
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, String title, Mirror<PlayerInventorySlot> mirror) {
        MainNmsInventory spectatorInv = new MainNmsInventory(((CraftHumanEntity) player).getHandle(), title, mirror);
        MainBukkitInventory bukkitInventory = spectatorInv.bukkit();
        InventoryView targetView = player.getOpenInventory();
        bukkitInventory.watch(targetView);
        return cache(bukkitInventory);
    }

    @Override
    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, Mirror<PlayerInventorySlot> mirror) {
        return createOffline(playerId, playerName, title, mirror, this::spectateInventory);
    }

    @Override
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory newInventory) {
        return save(newInventory, this::spectateInventory, MainSpectatorInventory::setContents);
    }

    @Override
    public void openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory inv, String title, Mirror<EnderChestSlot> mirror) {
        CraftPlayer bukkitPlayer = (CraftPlayer) spectator;
        ServerPlayer nmsPlayer = bukkitPlayer.getHandle();
        EnderBukkitInventory bukkitInventory = (EnderBukkitInventory) inv;
        EnderNmsInventory nmsInventory = bukkitInventory.getInventory();

        //this is what the nms does: nmsPlayer.openMenu(nmsWindow);
        //so let's emulate that!
        int windowId = nmsPlayer.nextContainerCounter();
        Inventory bottom = nmsPlayer.getInventory();
        EnderNmsContainer nmsWindow = new EnderNmsContainer(windowId, nmsInventory, bottom, nmsPlayer, mirror);
        nmsWindow.setTitle(CraftChatMessage.fromString(title != null ? title : inv.getTitle())[0]);
        boolean eventCancelled = CraftEventFactory.callInventoryOpenEvent(nmsPlayer, nmsWindow, false) == null; //closes current open inventory if one is already open
        if (!eventCancelled) {
            nmsPlayer.containerMenu = nmsWindow;
            nmsPlayer.connection.send(new ClientboundOpenScreenPacket(windowId, nmsWindow.getType(), nmsWindow.getTitle()));
            nmsPlayer.initMenu(nmsWindow);
        }
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title, Mirror<EnderChestSlot> mirror) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        CraftInventory craftInventory = (CraftInventory) player.getEnderChest();
        PlayerEnderChestContainer nmsInventory = (PlayerEnderChestContainer) craftInventory.getInventory();
        EnderNmsInventory spectatorInv = new EnderNmsInventory(uuid, name, nmsInventory.items, title, mirror);
        EnderBukkitInventory bukkitInventory = spectatorInv.bukkit();
        return cache(bukkitInventory);
    }

    @Override
    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID player, String name, String title, Mirror<EnderChestSlot> mirror) {
        return createOffline(player, name, title, mirror, this::spectateEnderChest);
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory newInventory) {
        return save(newInventory, this::spectateEnderChest, EnderSpectatorInventory::setContents);
    }

    private <Slot, IS extends SpectatorInventory<Slot>> CompletableFuture<Optional<IS>> createOffline(UUID player, String name, String title, Mirror<Slot> mirror, TriFunction<? super HumanEntity, String, ? super Mirror<Slot>, IS> invCreator) {
        CraftServer server = (CraftServer) plugin.getServer();
    	DedicatedPlayerList playerList = server.getHandle();
    	PlayerDataStorage worldNBTStorage = playerList.playerIo;
    	
    	CraftWorld world = (CraftWorld) server.getWorlds().get(0);
    	Location spawn = world.getSpawnLocation();
    	float yaw = spawn.getYaw();
    	GameProfile gameProfile = new GameProfile(player, name);
        ProfilePublicKey profilePublicKey = null;
        //ProfilePublicKey is only ever non-null for 'real' online players, used for the new chat reporting system.
        //For the purposes of loading nbt data, it is fine to leave this null.
    	
    	FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
    			world.getHandle(),
    			new BlockPos(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()),
    			yaw,
    			gameProfile,
                profilePublicKey);
    	
    	return CompletableFuture.supplyAsync(() -> {
    		CompoundTag playerCompound = worldNBTStorage.load(fakeEntityHuman);
    		if (playerCompound != null) {
    			fakeEntityHuman.readAdditionalSaveData(playerCompound);
    		} //else: player save file exists.
    		
    		CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
    		return Optional.of(invCreator.apply(craftHumanEntity, title, mirror));
    	}, serverThreadExecutor);   //loading must occur on the main thread.
    }

    private <Slot, SI extends SpectatorInventory<Slot>> CompletableFuture<Void> save(SI newInventory, TriFunction<? super HumanEntity, String, ? super Mirror<Slot>, SI> currentInvProvider, BiConsumer<SI, SI> transfer) {
        CraftServer server = (CraftServer) plugin.getServer();

    	CraftWorld world = (CraftWorld) server.getWorlds().get(0);
    	GameProfile gameProfile = new GameProfile(newInventory.getSpectatedPlayerId(), newInventory.getSpectatedPlayerName());
        ProfilePublicKey profilePublicKey = null;
        //ProfilePublicKey is only ever non-null for 'real' online players, used for the new chat reporting system.
        //For the purposes of saving player data, it is fine to leave this null.

        FakeEntityPlayer fakeEntityPlayer = new FakeEntityPlayer(
    			server.getServer(),
    			world.getHandle(),
    			gameProfile,
                profilePublicKey);
    	
    	return CompletableFuture.runAsync(() -> {
            FakeCraftPlayer fakeCraftPlayer = fakeEntityPlayer.getBukkitEntity();
            fakeCraftPlayer.loadData();

    		SI currentInv = currentInvProvider.apply(fakeCraftPlayer, newInventory.getTitle(), newInventory.getMirror());
    		transfer.accept(currentInv, newInventory);

            fakeCraftPlayer.saveData();
    	}, serverThreadExecutor);   //saving must occur on the main thread.
    }


}
