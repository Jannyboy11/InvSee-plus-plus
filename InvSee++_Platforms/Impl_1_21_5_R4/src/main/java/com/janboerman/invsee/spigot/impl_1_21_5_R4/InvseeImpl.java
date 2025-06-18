package com.janboerman.invsee.spigot.impl_1_21_5_R4;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.event.SpectatorInventorySaveEvent;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderGroup;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.NotOpenedReason;
import com.janboerman.invsee.spigot.api.response.OpenResponse;
import com.janboerman.invsee.spigot.api.response.SaveResponse;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.EventHelper;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.v1_21_R4.CraftServer;
import org.bukkit.craftbukkit.v1_21_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R4.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class InvseeImpl implements InvseePlatform {

    private final Plugin plugin;
    private final OpenSpectatorsCache cache;
    private final Scheduler scheduler;

    public InvseeImpl(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.scheduler = scheduler;

        if (lookup.onlineMode(plugin.getServer())) {
            lookup.uuidResolveStrategies.add(new UUIDSearchSaveFilesStrategy(plugin, scheduler));
        } else {
            // If we are in offline mode, then we should insert this strategy *before* the UUIDOfflineModeStrategy.
            lookup.uuidResolveStrategies.add(lookup.uuidResolveStrategies.size() - 1, new UUIDSearchSaveFilesStrategy(plugin, scheduler));
        }
        lookup.nameResolveStrategies.add(2, new NameSearchSaveFilesStrategy(plugin, scheduler));
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
        int windowId = HybridServerSupport.nextContainerCounter(nmsPlayer);
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
            MainBukkitInventoryView bukkitWindow = nmsWindow.getBukkitView();

            //send placeholders (inaccessible, armour, offhand, cursor, personal)
            Mirror<PlayerInventorySlot> mirror = options.getMirror();
            PlaceholderPalette palette = options.getPlaceholderPalette();
            ItemStack inaccessible = CraftItemStack.asNMSCopy(palette.inaccessible());
            for (int i = PlayerInventorySlot.CONTAINER_35.defaultIndex() + 1; i < nmsInventory.getContainerSize(); i++) {
                Integer idx = mirror.getIndex(PlayerInventorySlot.byDefaultIndex(i));
                if (idx == null) {
                    sendItemChange(nmsPlayer, i, inaccessible);
                    continue;
                }
                int rawIndex = idx.intValue();

                Slot slot = nmsWindow.getSlot(rawIndex);
                if (slot.hasItem()) continue;

                //slot has no item, send placeholder.
                if (slot instanceof InaccessibleSlot) sendItemChange(nmsPlayer, rawIndex, inaccessible);
                else if (slot instanceof BootsSlot) sendItemChange(nmsPlayer, rawIndex, CraftItemStack.asNMSCopy(palette.armourBoots()));
                else if (slot instanceof LeggingsSlot) sendItemChange(nmsPlayer, rawIndex, CraftItemStack.asNMSCopy(palette.armourLeggings()));
                else if (slot instanceof ChestplateSlot) sendItemChange(nmsPlayer, rawIndex, CraftItemStack.asNMSCopy(palette.armourChestplate()));
                else if (slot instanceof HelmetSlot) sendItemChange(nmsPlayer, rawIndex, CraftItemStack.asNMSCopy(palette.armourHelmet()));
                else if (slot instanceof OffhandSlot) sendItemChange(nmsPlayer, rawIndex, CraftItemStack.asNMSCopy(palette.offHand()));
                else if (slot instanceof CursorSlot) sendItemChange(nmsPlayer, rawIndex, CraftItemStack.asNMSCopy(palette.cursor()));
                else if (slot instanceof PersonalSlot personal) sendItemChange(nmsPlayer, rawIndex, personal.works() ? CraftItemStack.asNMSCopy(palette.generic()) : inaccessible);
            }

            //finally, return
            return OpenResponse.open(bukkitWindow);
        }
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, CreationOptions<PlayerInventorySlot> options) {
        MainNmsInventory spectatorInv = new MainNmsInventory(((CraftHumanEntity) player).getHandle(), options);
        MainBukkitInventory bukkitInventory = spectatorInv.bukkit();
        InventoryView targetView = player.getOpenInventory();
        bukkitInventory.watch(targetView);
        cache.cache(bukkitInventory);
        return bukkitInventory;
    }

    @Override
    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, CreationOptions<PlayerInventorySlot> options) {
        return createOffline(playerId, playerName, options, this::spectateInventory);
    }

    @Override
    public CompletableFuture<SaveResponse> saveInventory(MainSpectatorInventory newInventory) {
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
        int windowId = HybridServerSupport.nextContainerCounter(nmsPlayer);
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
        EnderNmsInventory spectatorInv = new EnderNmsInventory(((CraftHumanEntity) player).getHandle(), options);
        EnderBukkitInventory bukkitInventory = spectatorInv.bukkit();
        cache.cache(bukkitInventory);
        return bukkitInventory;
    }

    @Override
    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, CreationOptions<EnderChestSlot> options) {
        return createOffline(playerId, playerName, options, this::spectateEnderChest);
    }

    @Override
    public CompletableFuture<SaveResponse> saveEnderChest(EnderSpectatorInventory newInventory) {
        return save(newInventory, this::spectateEnderChest, EnderSpectatorInventory::setContents);
    }

    private <Slot, IS extends SpectatorInventory<Slot>> CompletableFuture<SpectateResponse<IS>> createOffline(UUID player, String name, CreationOptions<Slot> options, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, IS> invCreator) {

        CraftServer server = (CraftServer) plugin.getServer();
    	DedicatedPlayerList playerList = server.getHandle();
    	PlayerDataStorage worldNBTStorage = playerList.playerIo;
    	
    	CraftWorld world = (CraftWorld) server.getWorlds().get(0);
    	Location spawn = world.getSpawnLocation();
    	float yaw = spawn.getYaw();
    	GameProfile gameProfile = new GameProfile(player, name);
    	
    	FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
    			world.getHandle(),
    			new BlockPos(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()),
    			yaw,
    			gameProfile);
    	
    	return CompletableFuture.supplyAsync(() -> {
    		Optional<CompoundTag> playerCompound = worldNBTStorage.load(fakeEntityHuman);
            if (playerCompound.isEmpty()) {
                // player file does not exist
                if (!options.isUnknownPlayerSupported()) {
                    return SpectateResponse.fail(NotCreatedReason.unknownTarget(Target.byGameProfile(player, name)));
                } //else: unknown/new players are supported!
                // if we get here, then we create a spectator inventory for the non-existent player anyway.
            } else {
                // player file already exists, load the data from the compound onto the player
                fakeEntityHuman.readAdditionalSaveData(playerCompound.get());   //only player-specific stuff
                //fakeEntityHuman.load(playerCompound.get());                   //ALL entity data
            }

    		CraftHumanEntity craftHumanEntity = new FakeCraftHumanEntity(server, fakeEntityHuman);
            return SpectateResponse.succeed(EventHelper.callSpectatorInventoryOfflineCreatedEvent(server, invCreator.apply(craftHumanEntity, options)));
    	}, runnable -> scheduler.executeSyncPlayer(player, runnable, null));
    }

    private <Slot, SI extends SpectatorInventory<Slot>> CompletableFuture<SaveResponse> save(SI newInventory, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, SI> currentInvProvider, BiConsumer<SI, SI> transfer) {
        CraftServer server = (CraftServer) plugin.getServer();
        SpectatorInventorySaveEvent event = EventHelper.callSpectatorInventorySaveEvent(server, newInventory);
        if (event.isCancelled()) return CompletableFuture.completedFuture(SaveResponse.notSaved(newInventory));

    	CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        UUID playerId = newInventory.getSpectatedPlayerId();
        GameProfile gameProfile = new GameProfile(playerId, newInventory.getSpectatedPlayerName());
        ClientInformation clientInformation = ClientInformation.createDefault();

        FakeEntityPlayer fakeEntityPlayer = new FakeEntityPlayer(
    			server.getServer(),
    			world.getHandle(),
    			gameProfile,
                clientInformation);
    	
    	return CompletableFuture.supplyAsync(() -> {
            FakeCraftPlayer fakeCraftPlayer = fakeEntityPlayer.getBukkitEntity();
            fakeCraftPlayer.loadData();
            loadWorldDataAndGameMode(server, fakeEntityPlayer); //workaround for https://github.com/PaperMC/Paper/issues/11572

            CreationOptions<Slot> creationOptions = newInventory.getCreationOptions();
            SI currentInv = currentInvProvider.apply(fakeCraftPlayer, creationOptions);
            transfer.accept(currentInv, newInventory);

            fakeCraftPlayer.saveData();
            return SaveResponse.saved(currentInv);
    	}, runnable -> scheduler.executeSyncPlayer(playerId, runnable, null));
    }

    private void loadWorldDataAndGameMode(CraftServer server, FakeEntityPlayer fakeEntityPlayer) {
        // In Paper, Entity#load(CompoundTag) does not load the world info.
        // Thus, in order to not upset our users, we do it ourselves manually in order to work around this Paper bug.
        // See https://github.com/Jannyboy11/InvSee-plus-plus/issues/105.
        // See PaperMC/PlayerList#placeNewPlayer.

        PlayerDataStorage playerDataStorage = server.getHandle().playerIo;
        Optional<CompoundTag> optional = playerDataStorage.load(fakeEntityPlayer);

        if (optional.isPresent()) {
            ServerLevel level;
            CompoundTag nbttagcompound = optional.get();

            org.bukkit.World bWorld = null;
            if (nbttagcompound.contains("WorldUUIDMost") && nbttagcompound.contains("WorldUUIDLeast")) {
                // The main way for bukkit worlds to store the world is the world UUID despite mojang adding custom worlds
                bWorld = server.getWorld(new UUID(nbttagcompound.getLong("WorldUUIDMost").get(), nbttagcompound.getLong("WorldUUIDLeast").get()));
            } else if (nbttagcompound.contains("world")) { // legacy bukkit world name
                bWorld = server.getWorld(nbttagcompound.getString("world").get());
            }

            if (bWorld != null) {
                level = ((CraftWorld) bWorld).getHandle();
                fakeEntityPlayer.setServerLevel(level);
            } else {
                DataResult<ResourceKey<Level>> dataresult = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, nbttagcompound.get("Dimension")));
                Optional<ResourceKey<Level>> optionalLevelKey = dataresult.resultOrPartial(message -> plugin.getLogger().severe(message));
                ResourceKey<Level> levelResourceKey = optionalLevelKey.orElse(Level.OVERWORLD);
                level = server.getHandle().getServer().getLevel(levelResourceKey);

                if (level != null) {
                    fakeEntityPlayer.spawnIn(level, true/*ignore respawn anchor charge*/); //note: not only sets the ServerLevel, also sets x/y/z coordinates and gamemode.
                }
            }

            fakeEntityPlayer.loadGameTypes(nbttagcompound);
        }
    }

    private static Optional<InventoryOpenEvent> callInventoryOpenEvent(ServerPlayer nmsPlayer, AbstractContainerMenu nmsView) {
        //copy-pasta from CraftEventFactory, but returns the cancelled event in case it was cancelled.
        if (nmsPlayer.containerMenu != nmsPlayer.inventoryMenu) {
            nmsPlayer.connection.handleContainerClose(new ServerboundContainerClosePacket(nmsPlayer.containerMenu.containerId));
        }

        CraftServer server = nmsPlayer.getServer().server;
        CraftPlayer bukkitPlayer = nmsPlayer.getBukkitEntity();
        nmsPlayer.containerMenu.transferTo(nmsView, bukkitPlayer);
        InventoryOpenEvent event = new InventoryOpenEvent(nmsView.getBukkitView());
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            nmsView.transferTo(nmsPlayer.containerMenu, bukkitPlayer);
            return Optional.of(event);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public PlaceholderPalette getPlaceholderPalette(String name) {
        return switch (name) {
            case "glass panes" -> Placeholders.PALETTE_GLASS;
            case "icons" -> Placeholders.PALETTE_ICONS;
            default -> PlaceholderPalette.empty();
        };
    }

    static void sendItemChange(ServerPlayer entityPlayer, int rawIndex, ItemStack toSend) {
        AbstractContainerMenu container = entityPlayer.containerMenu;
        entityPlayer.connection.send(new ClientboundContainerSetSlotPacket(container.containerId, container.incrementStateId(), rawIndex, toSend));
    }

    static ItemStack getItemOrPlaceholder(PlaceholderPalette palette, MainBukkitInventoryView view, int rawIndex, PlaceholderGroup group) {
        Slot slot = view.nms.getSlot(rawIndex);
        if (slot.hasItem()) return slot.getItem();

        if (slot instanceof InaccessibleSlot) {
            return CraftItemStack.asNMSCopy(palette.inaccessible());
        } else if (slot instanceof BootsSlot) {
            return CraftItemStack.asNMSCopy(palette.armourBoots());
        } else if (slot instanceof LeggingsSlot) {
            return CraftItemStack.asNMSCopy(palette.armourLeggings());
        } else if (slot instanceof ChestplateSlot) {
            return CraftItemStack.asNMSCopy(palette.armourChestplate());
        } else if (slot instanceof HelmetSlot) {
            return CraftItemStack.asNMSCopy(palette.armourLeggings());
        } else if (slot instanceof OffhandSlot) {
            return CraftItemStack.asNMSCopy(palette.offHand());
        } else if (slot instanceof CursorSlot) {
            return CraftItemStack.asNMSCopy(palette.cursor());
        } else if (slot instanceof PersonalSlot personalSlot) {
            if (!personalSlot.works()) return CraftItemStack.asNMSCopy(palette.inaccessible());
            if (group == null) return ItemStack.EMPTY; //no group for personal slot -> fall back to empty stack

            Mirror<PlayerInventorySlot> mirror = view.nms.creationOptions.getMirror();
            PlayerInventorySlot pis = mirror.getSlot(rawIndex);
            if (pis == null) return CraftItemStack.asNMSCopy(palette.inaccessible());

            return CraftItemStack.asNMSCopy(palette.getPersonalSlotPlaceholder(pis, group));
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public Stream<Material> materials() {
        //https://discord.com/channels/289587909051416579/1077385604012179486/1263418959554805843
        Stream<Material> res;
        Registry<Material> registry = Bukkit.getRegistry(Material.class);
        if (registry != null) {
            res = registry.stream();
        } else {
            res = Registry.MATERIAL.stream();
        }
        // Note: in the future, Registry.ITEM may become a stable api. We prefer to use that one since we don't care
        // about Block materials; we only care about Item materials.
        return res.filter(Material::isItem);
    }
}
