package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.Title;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_16_R3.*;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class InvseeImpl extends InvseeAPI {

    static final ItemStack EMPTY_STACK = ItemStack.b;

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
    public InventoryView openMainSpectatorInventory(Player spectator, MainSpectatorInventory inv, CreationOptions<PlayerInventorySlot> options) {
        var target = Target.byGameProfile(inv.getSpectatedPlayerId(), inv.getSpectatedPlayerName());
        var title = options.getTitle().titleFor(target);
        var mirror = options.getMirror();

        CraftPlayer bukkitPlayer = (CraftPlayer) spectator;
        EntityPlayer nmsPlayer = bukkitPlayer.getHandle();
        MainBukkitInventory bukkitInventory = (MainBukkitInventory) inv;
        MainNmsInventory nmsInventory = bukkitInventory.getInventory();

        //this is what the nms does: nmsPlayer.openContainer(nmsWindow);
        //so let's emulate that!
        int windowId = nmsPlayer.nextContainerCounter();
        PlayerInventory bottom = nmsPlayer.inventory;
        MainNmsContainer nmsWindow = new MainNmsContainer(windowId, nmsInventory, bottom, nmsPlayer, mirror);
        nmsWindow.setTitle(CraftChatMessage.fromString(title != null ? title : inv.getTitle())[0]);
        boolean eventCancelled = CraftEventFactory.callInventoryOpenEvent(nmsPlayer, nmsWindow, false) == null; //closes current open inventory if one is already open
        if (eventCancelled) {
            return null;
        } else {
            nmsPlayer.activeContainer = nmsWindow;
            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, nmsWindow.getType(), nmsWindow.getTitle()));
            nmsWindow.addSlotListener(nmsPlayer);
            return nmsWindow.getBukkitView();
        }
    }

    @Override
    public InventoryView openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory inv, CreationOptions<EnderChestSlot> options) {
        var target = Target.byGameProfile(inv.getSpectatedPlayerId(), inv.getSpectatedPlayerName());
        var title = options.getTitle().titleFor(target);
        var mirror = options.getMirror();

        CraftPlayer bukkitPlayer = (CraftPlayer) spectator;
        EntityPlayer nmsPlayer = bukkitPlayer.getHandle();
        EnderBukkitInventory bukkitInventory = (EnderBukkitInventory) inv;
        EnderNmsInventory nmsInventory = bukkitInventory.getInventory();

        //this is what the nms does: nmsPlayer.openContainer(nmsWindow);
        //so let's emulate that!
        int windowId = nmsPlayer.nextContainerCounter();
        PlayerInventory bottom = nmsPlayer.inventory;
        EnderNmsContainer nmsWindow = new EnderNmsContainer(windowId, nmsInventory, bottom, nmsPlayer, mirror);
        nmsWindow.setTitle(CraftChatMessage.fromString(title != null ? title : inv.getTitle())[0]);
        boolean eventCancelled = CraftEventFactory.callInventoryOpenEvent(nmsPlayer, nmsWindow, false) == null; //closes current open inventory if one is already open
        if (eventCancelled) {
            return null;
        } else {
            nmsPlayer.activeContainer = nmsWindow;
            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, nmsWindow.getType(), nmsWindow.getTitle()));
            nmsWindow.addSlotListener(nmsPlayer);
            return nmsWindow.getBukkitView();
        }
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, CreationOptions<PlayerInventorySlot> options) {
        MainNmsInventory spectatorInv = new MainNmsInventory(((CraftHumanEntity) player).getHandle(), options.getTitle().titleFor(Target.byPlayer(player)), options.getMirror());
        MainBukkitInventory bukkitInventory = spectatorInv.bukkit();
        InventoryView targetView = player.getOpenInventory();
        bukkitInventory.watch(targetView);
        cache(bukkitInventory);
        return bukkitInventory;
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, CreationOptions<EnderChestSlot> options) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        CraftInventory craftInventory = (CraftInventory) player.getEnderChest();
        InventoryEnderChest nmsInventory = (InventoryEnderChest) craftInventory.getInventory();
        EnderNmsInventory spectatorInv = new EnderNmsInventory(uuid, name, nmsInventory.items, options.getTitle().titleFor(Target.byPlayer(player)), options.getMirror());
        EnderBukkitInventory bukkitInventory = spectatorInv.bukkit();
        cache(bukkitInventory);
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
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory newInventory) {
        return save(newInventory, this::spectateInventory, MainSpectatorInventory::setContents);
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory newInventory) {
        return save(newInventory, this::spectateEnderChest, EnderSpectatorInventory::setContents);
    }

    private <Slot, IS extends SpectatorInventory<Slot>> CompletableFuture<SpectateResponse<IS>> createOffline(UUID player, String name, CreationOptions<Slot> options, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, IS> invCreator) {

        CraftServer server = (CraftServer) plugin.getServer();
        DedicatedPlayerList playerList = server.getHandle();
        WorldNBTStorage worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        float yaw = 0F;
        GameProfile gameProfile = new GameProfile(player, name);

        FakeEntityHuman fakeEntityHuman = new FakeEntityHuman(
                world.getHandle(),
                new BlockPosition(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()),
                yaw,
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
                fakeEntityHuman.loadData(playerCompound);   //only player-specific stuff
            }

            CraftHumanEntity craftHumanEntity = new CraftHumanEntity(server, fakeEntityHuman);
            return SpectateResponse.succeed(invCreator.apply(craftHumanEntity, options));
        }, serverThreadExecutor);
    }

    private <Slot, SI extends SpectatorInventory<Slot>> CompletableFuture<Void> save(SI newInventory, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, SI> currentInvProvider, BiConsumer<SI, SI> transfer) {

        CraftServer server = (CraftServer) plugin.getServer();
        DedicatedPlayerList playerList = server.getHandle();
        WorldNBTStorage worldNBTStorage = playerList.playerFileData;

        CraftWorld world = (CraftWorld) server.getWorlds().get(0);
        GameProfile gameProfile = new GameProfile(newInventory.getSpectatedPlayerId(), newInventory.getSpectatedPlayerName());

        FakeEntityPlayer fakeEntityPlayer = new FakeEntityPlayer(
                server.getServer(),
                world.getHandle(),
                gameProfile,
                new PlayerInteractManager(world.getHandle()));

        return CompletableFuture.runAsync(() -> {
            NBTTagCompound playerCompound = worldNBTStorage.load(fakeEntityPlayer);
            if (playerCompound != null) {
                fakeEntityPlayer.load(playerCompound);   //all entity stuff + player stuff
            } //else: no player save file exists

            FakeCraftPlayer craftHumanEntity = fakeEntityPlayer.getBukkitEntity();
            CreationOptions<Slot> creationOptions = CreationOptions.of(
                    Title.of(newInventory.getTitle()),
                    true,
                    newInventory.getMirror(),
                    true);
            SI currentInv = currentInvProvider.apply(craftHumanEntity, creationOptions);

            transfer.accept(currentInv, newInventory);

            worldNBTStorage.save(fakeEntityPlayer);
        }, serverThreadExecutor);
    }

}
