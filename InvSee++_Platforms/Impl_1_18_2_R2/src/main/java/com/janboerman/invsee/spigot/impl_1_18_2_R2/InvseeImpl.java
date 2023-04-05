package com.janboerman.invsee.spigot.impl_1_18_2_R2;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.NotOpenedReason;
import com.janboerman.invsee.spigot.api.response.OpenResponse;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import static com.janboerman.invsee.spigot.impl_1_18_2_R2.HybridServerSupport.enderChestItems;
import static com.janboerman.invsee.spigot.impl_1_18_2_R2.HybridServerSupport.nextContainerCounter;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class InvseeImpl extends InvseeAPI implements InvseePlatform {

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
    protected InvseePlatform getPlatform() {
        return this;
    }

    @Override
    public OpenResponse<MainSpectatorInventoryView> openMainSpectatorInventory(Player spectator, MainSpectatorInventory inv, CreationOptions<PlayerInventorySlot> options) {
        var target = Target.byGameProfile(inv.getSpectatedPlayerId(), inv.getSpectatedPlayerName());
        var title = options.getTitle().titleFor(target);

        CraftPlayer bukkitPlayer = (CraftPlayer) spectator;
        ServerPlayer nmsPlayer = bukkitPlayer.getHandle();
        MainBukkitInventory bukkitInventory = (MainBukkitInventory) inv;
        MainNmsInventory nmsInventory = bukkitInventory.getInventory();

        //this is what the nms does: nmsPlayer.openMenu(nmsWindow);
        //so let's emulate that!
        int windowId = nextContainerCounter(nmsPlayer);
        Inventory bottom = nmsPlayer.getInventory();
        MainNmsContainer nmsWindow = new MainNmsContainer(windowId, nmsInventory, bottom, nmsPlayer, options);
        nmsWindow.setTitle(CraftChatMessage.fromString(title != null ? title : inv.getTitle())[0]);
        var eventCancelled = callInventoryOpenEvent(nmsPlayer, nmsWindow); //closes current open inventory if one is already open
        if (eventCancelled.isPresent()) {
            return OpenResponse.closed(NotOpenedReason.inventoryOpenEventCancelled(eventCancelled.get()));
        } else {
            nmsPlayer.containerMenu = nmsWindow;
            nmsPlayer.connection.send(new ClientboundOpenScreenPacket(windowId, nmsWindow.getType(), nmsWindow.getTitle()));
            nmsPlayer.initMenu(nmsWindow);
            return OpenResponse.open(nmsWindow.getBukkitView());
        }
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, CreationOptions<PlayerInventorySlot> options) {
        MainNmsInventory spectatorInv = new MainNmsInventory(((CraftHumanEntity) player).getHandle(), options);
        MainBukkitInventory bukkitInventory = spectatorInv.bukkit();
        InventoryView targetView = player.getOpenInventory();
        bukkitInventory.watch(targetView);
        cache(bukkitInventory);
        return bukkitInventory;
    }

    @Override
    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, CreationOptions<PlayerInventorySlot> options) {
        return createOffline(playerId, playerName, options, this::spectateInventory);
    }

    @Override
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory newInventory) {
        return save(newInventory, this::spectateInventory, MainSpectatorInventory::setContents);
    }

    @Override
    public OpenResponse<EnderSpectatorInventoryView> openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory inv, CreationOptions<EnderChestSlot> options) {
        var target = Target.byGameProfile(inv.getSpectatedPlayerId(), inv.getSpectatedPlayerName());
        var title = options.getTitle().titleFor(target);

        CraftPlayer bukkitPlayer = (CraftPlayer) spectator;
        ServerPlayer nmsPlayer = bukkitPlayer.getHandle();
        EnderBukkitInventory bukkitInventory = (EnderBukkitInventory) inv;
        EnderNmsInventory nmsInventory = bukkitInventory.getInventory();

        //this is what the nms does: nmsPlayer.openMenu(nmsWindow);
        //so let's emulate that!
        int windowId = nextContainerCounter(nmsPlayer);
        Inventory bottom = nmsPlayer.getInventory();
        EnderNmsContainer nmsWindow = new EnderNmsContainer(windowId, nmsInventory, bottom, nmsPlayer, options);
        nmsWindow.setTitle(CraftChatMessage.fromString(title != null ? title : inv.getTitle())[0]);
        var eventCancelled = callInventoryOpenEvent(nmsPlayer, nmsWindow); //closes current open inventory if one is already open
        if (eventCancelled.isPresent()) {
            return OpenResponse.closed(NotOpenedReason.inventoryOpenEventCancelled(eventCancelled.get()));
        } else {
            nmsPlayer.containerMenu = nmsWindow;
            nmsPlayer.connection.send(new ClientboundOpenScreenPacket(windowId, nmsWindow.getType(), nmsWindow.getTitle()));
            nmsPlayer.initMenu(nmsWindow);
            return OpenResponse.open(nmsWindow.getBukkitView());
        }
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, CreationOptions<EnderChestSlot> options) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        CraftInventory craftInventory = (CraftInventory) player.getEnderChest();
        PlayerEnderChestContainer nmsInventory = (PlayerEnderChestContainer) craftInventory.getInventory();
        EnderNmsInventory spectatorInv = new EnderNmsInventory(uuid, name, enderChestItems(nmsInventory), options);
        EnderBukkitInventory bukkitInventory = spectatorInv.bukkit();
        cache(bukkitInventory);
        return bukkitInventory;
    }

    @Override
    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, CreationOptions<EnderChestSlot> options) {
        return createOffline(playerId, playerName, options, this::spectateEnderChest);
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory newInventory) {
        return save(newInventory, this::spectateEnderChest, EnderSpectatorInventory::setContents);
    }

    private <Slot, IS extends SpectatorInventory<Slot>> CompletableFuture<SpectateResponse<IS>> createOffline(UUID player, String name, CreationOptions<Slot> options, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, IS> invCreator) {

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
            if (playerCompound == null) {
                // player file does not exist
                if (!options.isUnknownPlayerSupported()) {
                    return SpectateResponse.fail(NotCreatedReason.unknownTarget(Target.byGameProfile(player, name)));
                } //else: unknown/new players are supported!
                // if we get here, then we create a spectator inventory for the non-existent player anyway.
            } else {
                // player file already exists, load the data from the compound onto the player
                fakeEntityHuman.readAdditionalSaveData(playerCompound);		//only player-specific stuff
            }

            CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
            return SpectateResponse.succeed(invCreator.apply(craftHumanEntity, options));
    	}, serverThreadExecutor);
    }

    private <Slot, SI extends SpectatorInventory<Slot>> CompletableFuture<Void> save(SI newInventory, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, SI> currentInvProvider, BiConsumer<SI, SI> transfer) {

        CraftServer server = (CraftServer) plugin.getServer();

    	CraftWorld world = (CraftWorld) server.getWorlds().get(0);
    	GameProfile gameProfile = new GameProfile(newInventory.getSpectatedPlayerId(), newInventory.getSpectatedPlayerName());

        FakeEntityPlayer fakeEntityPlayer = new FakeEntityPlayer(
    			server.getServer(),
    			world.getHandle(),
    			gameProfile);
    	
    	return CompletableFuture.runAsync(() -> {
            FakeCraftPlayer fakeCraftPlayer = fakeEntityPlayer.getBukkitEntity();
            fakeCraftPlayer.loadData();

            CreationOptions<Slot> creationOptions = newInventory.getCreationOptions();
    		SI currentInv = currentInvProvider.apply(fakeCraftPlayer, creationOptions);
    		transfer.accept(currentInv, newInventory);

            fakeCraftPlayer.saveData();
    	}, serverThreadExecutor);
    }

    private static Optional<InventoryOpenEvent> callInventoryOpenEvent(ServerPlayer player, AbstractContainerMenu container) {
        //copy-pasta from CraftEventFactory
        if (player.containerMenu != player.inventoryMenu) {
            player.connection.handleContainerClose(new ServerboundContainerClosePacket(player.containerMenu.containerId));
        }

        CraftServer server = player.level.getCraftServer();
        CraftPlayer craftPlayer = player.getBukkitEntity();
        player.containerMenu.transferTo(container, craftPlayer);
        InventoryOpenEvent event = new InventoryOpenEvent(container.getBukkitView());
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            container.transferTo(player.containerMenu, craftPlayer);
            return Optional.of(event);
        } else {
            return Optional.empty();
        }
    }

}
