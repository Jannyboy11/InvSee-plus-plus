package com.janboerman.invsee.spigot.impl_1_12_R1;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.event.SpectatorInventorySaveEvent;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderGroup;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette;
import com.janboerman.invsee.spigot.api.response.*;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import static com.janboerman.invsee.spigot.impl_1_12_R1.HybridServerSupport.enderChestItems;
import static com.janboerman.invsee.spigot.impl_1_12_R1.HybridServerSupport.nextContainerCounter;

import com.janboerman.invsee.spigot.internal.EventHelper;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.Container;
import net.minecraft.server.v1_12_R1.DedicatedPlayerList;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.IPlayerFileData;
import net.minecraft.server.v1_12_R1.InventoryEnderChest;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayInCloseWindow;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_12_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.PlayerInventory;
import net.minecraft.server.v1_12_R1.Slot;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import net.minecraft.server.v1_12_R1.ItemStack;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class InvseeImpl implements InvseePlatform {

    static final ItemStack EMPTY_STACK = ItemStack.a;

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
        EntityPlayer nmsPlayer = bukkitPlayer.getHandle();
        MainBukkitInventory bukkitInventory = (MainBukkitInventory) inv;
        MainNmsInventory nmsInventory = bukkitInventory.getInventory();

        //this is what the nms does: nmsPlayer.openTileEntity(nmsWindow);
        //so let's emulate that!
        int windowId = nextContainerCounter(nmsPlayer);
        PlayerInventory bottom = nmsPlayer.inventory;
        MainNmsContainer nmsWindow = new MainNmsContainer(windowId, nmsInventory, bottom, nmsPlayer, options);
        IChatBaseComponent titleComponent = title != null ? CraftChatMessage.fromString(title)[0] : nmsInventory.getScoreboardDisplayName();
        var eventCancelled = callInventoryOpenEvent(nmsPlayer, nmsWindow); //closes current open inventory if one is already open
        if (eventCancelled.isPresent()) {
            return OpenResponse.closed(NotOpenedReason.inventoryOpenEventCancelled(eventCancelled.get()));
        } else {
            nmsPlayer.activeContainer = nmsWindow;
            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, nmsInventory.getContainerName(), titleComponent, nmsInventory.getSize()));
            nmsWindow.addSlotListener(nmsPlayer);

            //send placeholders
            Mirror<PlayerInventorySlot> mirror = options.getMirror();
            PlaceholderPalette palette = options.getPlaceholderPalette();
            ItemStack inaccessible = CraftItemStack.asNMSCopy(palette.inaccessible());
            for (int i = PlayerInventorySlot.CONTAINER_35.defaultIndex() + 1; i < nmsInventory.getSize(); i++) {
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
                else if (slot instanceof PersonalSlot) sendItemChange(nmsPlayer, rawIndex, ((PersonalSlot) slot).works() ? CraftItemStack.asNMSCopy(palette.generic()) : inaccessible);
            }

            return OpenResponse.open(nmsWindow.getBukkitView());
        }
    }

    @Override
    public OpenResponse<EnderSpectatorInventoryView> openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory inv, CreationOptions<EnderChestSlot> options) {
        var target = Target.byGameProfile(inv.getSpectatedPlayerId(), inv.getSpectatedPlayerName());
        var title = options.getTitle().titleFor(target);

        CraftPlayer bukkitPlayer = (CraftPlayer) spectator;
        EntityPlayer nmsPlayer = bukkitPlayer.getHandle();
        EnderBukkitInventory bukkitInventory = (EnderBukkitInventory) inv;
        EnderNmsInventory nmsInventory = bukkitInventory.getInventory();

        //this is what the nms does: nmsPlayer.openTileEntity(nmsWindow);
        //so let's emulate that!
        int windowId = nextContainerCounter(nmsPlayer);
        PlayerInventory bottom = nmsPlayer.inventory;
        EnderNmsContainer nmsWindow = new EnderNmsContainer(windowId, nmsInventory, bottom, nmsPlayer, options);
        IChatBaseComponent titleComponent = title != null ? CraftChatMessage.fromString(title)[0] : nmsInventory.getScoreboardDisplayName();
        var eventCancelled = callInventoryOpenEvent(nmsPlayer, nmsWindow); //closes current open inventory if one is already open
        if (eventCancelled.isPresent()) {
            return OpenResponse.closed(NotOpenedReason.inventoryOpenEventCancelled(eventCancelled.get()));
        } else {
            nmsPlayer.activeContainer = nmsWindow;
            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, nmsInventory.getContainerName(), titleComponent, nmsInventory.getSize()));
            nmsWindow.addSlotListener(nmsPlayer);
            return OpenResponse.open(nmsWindow.getBukkitView());
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
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, CreationOptions<EnderChestSlot> options) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        CraftInventory craftInventory = (CraftInventory) player.getEnderChest();
        InventoryEnderChest nmsInventory = (InventoryEnderChest) craftInventory.getInventory();
        EnderNmsInventory spectatorInv = new EnderNmsInventory(uuid, name, enderChestItems(nmsInventory), options);
        EnderBukkitInventory bukkitInventory = spectatorInv.bukkit();
        cache.cache(bukkitInventory);
        return bukkitInventory;
    }

    @Override
    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, CreationOptions<PlayerInventorySlot> options) {
        return createOffline(playerId, playerName, options, this::spectateInventory);
    }

    @Override
    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, CreationOptions<EnderChestSlot> options) {
        return createOffline(playerId, playerName, options, this::spectateEnderChest);
    }

    @Override
    public CompletableFuture<SaveResponse> saveInventory(MainSpectatorInventory newInventory) {
        return save(newInventory, this::spectateInventory, MainSpectatorInventory::setContents);
    }

    @Override
    public CompletableFuture<SaveResponse> saveEnderChest(EnderSpectatorInventory newInventory) {
        return save(newInventory, this::spectateEnderChest, EnderSpectatorInventory::setContents);
    }

    private <Slot, IS extends SpectatorInventory<Slot>> CompletableFuture<SpectateResponse<IS>> createOffline(UUID player, String name, CreationOptions<Slot> options, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, IS> invCreator) {

        CraftServer server = (CraftServer) plugin.getServer();
        DedicatedPlayerList playerList = server.getHandle();
        IPlayerFileData worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        GameProfile gameProfile = new GameProfile(player, name);

        FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
                world.getHandle(),
                gameProfile);

        return CompletableFuture.supplyAsync(() -> {
            NBTTagCompound playerCompound = worldNBTStorage.load(fakeEntityHuman);
            if (playerCompound == null) {
                // player file does not exist
                if (!options.isUnknownPlayerSupported()) {
                    return SpectateResponse.fail(NotCreatedReason.unknownTarget(Target.byGameProfile(player, name)));
                } //else: unknown/new players are supported!
                // if we get here, then we create a spectator inventory for the non-existent player anyway.
            } else {
                // player file already exists, load the data from the compound onto the player
                fakeEntityHuman.a(playerCompound);   //only player-specific stuff
            }

            CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
            return SpectateResponse.succeed(EventHelper.callSpectatorInventoryOfflineCreatedEvent(server, invCreator.apply(craftHumanEntity, options)));

        }, runnable -> scheduler.executeSyncPlayer(player, runnable, null));
    }

    private <Slot, SI extends SpectatorInventory<Slot>> CompletableFuture<SaveResponse> save(SI newInventory, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, SI> currentInvProvider, BiConsumer<SI, SI> transfer) {

        CraftServer server = (CraftServer) plugin.getServer();
        SpectatorInventorySaveEvent event = EventHelper.callSpectatorInventorySaveEvent(server, newInventory);
        if (event.isCancelled()) return CompletableFuture.completedFuture(SaveResponse.notSaved(newInventory));

        DedicatedPlayerList playerList = server.getHandle();
        IPlayerFileData worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        UUID playerId = newInventory.getSpectatedPlayerId();
        GameProfile gameProfile = new GameProfile(playerId, newInventory.getSpectatedPlayerName());

        FakeEntityPlayer fakeEntityPlayer = new FakeEntityPlayer(
                server.getServer(),
                world.getHandle(),
                gameProfile,
                new PlayerInteractManager(world.getHandle()));

        return CompletableFuture.supplyAsync(() -> {
            NBTTagCompound playerCompound = worldNBTStorage.load(fakeEntityPlayer);
            if (playerCompound != null) {
                fakeEntityPlayer.f(playerCompound);   //all entity stuff + player stuff
            } //else: no player save file exists

            FakeCraftPlayer craftHumanEntity = fakeEntityPlayer.getBukkitEntity();
            CreationOptions<Slot> creationOptions = newInventory.getCreationOptions();
            SI currentInv = currentInvProvider.apply(craftHumanEntity, creationOptions);

            transfer.accept(currentInv, newInventory);

            worldNBTStorage.save(fakeEntityPlayer);
            return SaveResponse.saved(currentInv);
        }, runnable -> scheduler.executeSyncPlayer(playerId, runnable, null));
    }

    private static Optional<InventoryOpenEvent> callInventoryOpenEvent(EntityPlayer player, Container container) {
        //copy-pasta from CraftEventFactory
        if (player.activeContainer != player.defaultContainer) {
            player.playerConnection.a(new PacketPlayInCloseWindow(player.activeContainer.windowId));
        }

        CraftServer server = player.world.getServer();
        CraftPlayer craftPlayer = player.getBukkitEntity();
        player.activeContainer.transferTo(container, craftPlayer);
        InventoryOpenEvent event = new InventoryOpenEvent(container.getBukkitView());
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            container.transferTo(player.activeContainer, craftPlayer);
            return Optional.of(event);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette getPlaceholderPalette(String name) {
        switch (name) {
            case "glass panes": return Placeholders.PALETTE_GLASS;
            case "icons": return Placeholders.PALETTE_ICONS;
            default: return com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette.empty();
        }
    }

    static void sendItemChange(EntityPlayer entityPlayer, int rawIndex, ItemStack toSend) {
        Container container = entityPlayer.activeContainer;
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(container.windowId, rawIndex, toSend));
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
        } else if (slot instanceof PersonalSlot) {
            PersonalSlot personalSlot = (PersonalSlot) slot;
            if (!personalSlot.works()) return CraftItemStack.asNMSCopy(palette.inaccessible());
            if (group == null) return EMPTY_STACK; //no group for personal slot -> fall back to empty stack

            Mirror<PlayerInventorySlot> mirror = view.nms.creationOptions.getMirror();
            PlayerInventorySlot pis = mirror.getSlot(rawIndex);
            if (pis == null) return CraftItemStack.asNMSCopy(palette.inaccessible());

            return CraftItemStack.asNMSCopy(palette.getPersonalSlotPlaceholder(pis, group));
        } else {
            return EMPTY_STACK;
        }
    }
}
