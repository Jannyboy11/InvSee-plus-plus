package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.NotOpenedReason;
import com.janboerman.invsee.spigot.api.response.OpenResponse;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import com.janboerman.invsee.utils.Rethrow;
import io.netty.channel.Channel;
import net.glowstone.GlowServer;
import net.glowstone.entity.GlowHumanEntity;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.entity.meta.profile.GlowPlayerProfile;
import net.glowstone.io.PlayerDataService.PlayerReader;
import net.glowstone.io.entity.EntityStore;
import net.glowstone.io.nbt.NbtPlayerDataService;
import net.glowstone.net.GameServer;
import net.glowstone.net.GlowSession;
import net.glowstone.util.InventoryUtil;
import net.glowstone.util.nbt.CompoundTag;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class InvseeImpl implements InvseePlatform {

    static final ItemStack EMPTY_STACK = InventoryUtil.EMPTY_STACK;

    private final Plugin plugin;
    private final OpenSpectatorsCache cache;
    private final Scheduler scheduler;

    public InvseeImpl(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        GlowServer server = (GlowServer) plugin.getServer();

        this.plugin = plugin;
        this.cache = cache;
        this.scheduler = scheduler;

        if (lookup.onlineMode(plugin.getServer())) {
            lookup.uuidResolveStrategies.add(new UUIDSearchSaveFilesStrategy(plugin, scheduler));
        } else {
            lookup.uuidResolveStrategies.add(lookup.uuidResolveStrategies.size() - 1, new UUIDSearchSaveFilesStrategy(plugin, scheduler));
        }
        lookup.nameResolveStrategies.add(new NameSearchSaveFilesStrategy(plugin, scheduler));

        //add extra event listener for DifferenceTracker since Glowstone's InventoryView implementation does not get inventory clicks passed.
        server.getPluginManager().registerEvent(InventoryCloseEvent.class, new Listener() {}, EventPriority.MONITOR, (Listener listener, Event ev) -> {
            InventoryCloseEvent event = (InventoryCloseEvent) ev;
            InventoryView view = event.getView();
            if (view instanceof MainInventoryView) {
                ((MainInventoryView) view).onClose();
            } else if (view instanceof EnderInventoryView) {
                ((EnderInventoryView) view).onClose();
            }
        }, plugin);
        //add extra event listener to capture InventoryOpenEvents
        server.getPluginManager().registerEvent(InventoryOpenEvent.class, new Listener() {}, EventPriority.MONITOR, (Listener listener, Event ev) -> {
            InventoryOpenEvent event = (InventoryOpenEvent) ev;
            InventoryView view = event.getView();
            if (view instanceof MainInventoryView) {
                ((MainInventoryView) view).openEvent = event;
            } else if (view instanceof EnderInventoryView) {
                ((EnderInventoryView) view).openEvent = event;
            }
        }, plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (server.getNetworkServer() != null) {
                    GlowstoneHacks.injectWindowClickHandler(server);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity target, CreationOptions<PlayerInventorySlot> options) {
        MainInventory spectatorInv = new MainInventory((GlowHumanEntity) target, options);
        InventoryView targetView = target.getOpenInventory();
        spectatorInv.watch(targetView);
        cache.cache(spectatorInv);
        return spectatorInv;
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
    public OpenResponse<MainSpectatorInventoryView> openMainSpectatorInventory(Player spectator, MainSpectatorInventory inv, CreationOptions<PlayerInventorySlot> options) {
        MainInventory glowInventory = (MainInventory) inv;
        MainInventoryView view = new MainInventoryView(spectator, glowInventory, options);

        spectator.openInventory(view);

        if (view.openEvent != null && view.openEvent.isCancelled()) {
            return OpenResponse.closed(NotOpenedReason.inventoryOpenEventCancelled(view.openEvent));
        } else {
            return OpenResponse.open(view);
        }
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity target, CreationOptions<EnderChestSlot> options) {
        EnderInventory spectatorInv = new EnderInventory((GlowHumanEntity) target, options);
        cache.cache(spectatorInv);
        return spectatorInv;
    }

    @Override
    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, CreationOptions<EnderChestSlot> options) {
        return createOffline(playerId, playerName, options, this::spectateEnderChest);
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory newInventory) {
        return save(newInventory, this::spectateEnderChest, EnderSpectatorInventory::setContents);
    }

    @Override
    public OpenResponse<EnderSpectatorInventoryView> openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory inv, CreationOptions<EnderChestSlot> options) {
        EnderInventory glowInventory = (EnderInventory) inv;
        EnderInventoryView view = new EnderInventoryView(spectator, glowInventory, options);

        spectator.openInventory(view);

        if (view.openEvent != null && view.openEvent.isCancelled()) {
            return OpenResponse.closed(NotOpenedReason.inventoryOpenEventCancelled(view.openEvent));
        } else {
            return OpenResponse.open(view);
        }
    }

    private <Slot, IS extends SpectatorInventory<Slot>> CompletableFuture<SpectateResponse<IS>> createOffline(UUID playerId, String playerName, CreationOptions<Slot> options, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, IS> invCreator) {
        GlowServer server = (GlowServer) plugin.getServer();
        NbtPlayerDataService playerDataService = (NbtPlayerDataService) server.getPlayerDataService();

        // Steps:
        //1. Create a fake player
        //2. Load data onto it
        //3. Return the inventory!

        //create the fake player
        GameServer gameServer = server.getNetworkServer();
        Channel channel = FakeChannel.INSTANCE;
        GlowSession session = new GlowSession(server, gameServer.getProtocolProvider(), channel, gameServer);
        GlowPlayerProfile profile = new GlowPlayerProfile(playerName, playerId, true);
        PlayerReader reader = playerDataService.beginReadingData(playerId);
        FakePlayer fakePlayer = new FakePlayer(session, profile, reader);
        GlowstoneHacks.setPlayer(session, fakePlayer);

        return CompletableFuture.supplyAsync(() -> {
            //if the player's save file does not exist, then fail.
            File playerDataFolder = GlowstoneHacks.getPlayerDir(playerDataService);
            File playerFile = new File(playerDataFolder, playerId.toString() + ".dat");
            boolean playerFileExists = playerFile.exists() && playerFile.isFile();
            if (!playerFileExists && !options.isUnknownPlayerSupported()) {
                return SpectateResponse.fail(NotCreatedReason.unknownTarget(Target.byGameProfile(playerId, playerName)));
            }

            try {
                //load data onto the fake player
                CompoundTag tag = playerFileExists ? GlowstoneHacks.readCompressed(playerFile) : new CompoundTag();
                EntityStore glowhumanentityStore = GlowstoneHacks.findEntityStore(GlowPlayer.class, "load");
                glowhumanentityStore.load(fakePlayer, tag);

                //return the inventory
                return SpectateResponse.succeed(invCreator.apply(fakePlayer, options));
            } catch (IOException e) {
                return Rethrow.unchecked(e);
            }

        }, runnable -> scheduler.executeSyncPlayer(playerId, runnable, null));
    }

    private <Slot, SI extends SpectatorInventory<Slot>> CompletableFuture<Void> save(SI newInventory, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, SI> currentInvProvider, BiConsumer<SI, SI> transfer) {
        GlowServer server = (GlowServer) plugin.getServer();
        NbtPlayerDataService playerDataService = (NbtPlayerDataService) server.getPlayerDataService();
        UUID playerId = newInventory.getSpectatedPlayerId();
        String playerName = newInventory.getName();

        // Steps:
        //1. Create a fake player
        //2. Load data onto it
        //3. Transfer inventory onto the fake player
        //4. Save the fake player's data again

        //create the fake player
        GameServer gameServer = server.getNetworkServer();
        Channel channel = FakeChannel.INSTANCE;
        GlowSession session = new GlowSession(server, gameServer.getProtocolProvider(), channel, gameServer);
        GlowPlayerProfile profile = new GlowPlayerProfile(playerName, playerId, true);
        PlayerReader reader = playerDataService.beginReadingData(playerId);
        FakePlayer fakePlayer = new FakePlayer(session, profile, reader);
        GlowstoneHacks.setPlayer(session, fakePlayer);

        return CompletableFuture.runAsync(() -> {
            //get player file
            File playerDataFolder = GlowstoneHacks.getPlayerDir(playerDataService);
            File playerFile = new File(playerDataFolder, playerId.toString() + ".dat");
            boolean playerFileExists = playerFile.exists() && playerFile.isFile();
            try {
                //load
                CompoundTag tag = playerFileExists ? GlowstoneHacks.readCompressed(playerFile) : new CompoundTag();
                EntityStore glowplayerStore = GlowstoneHacks.findEntityStore(GlowPlayer.class, "load");
                glowplayerStore.load(fakePlayer, tag);

                //transfer inventory data
                SI currentInventory = currentInvProvider.apply(fakePlayer, newInventory.getCreationOptions());
                transfer.accept(currentInventory, newInventory);

                //save
                glowplayerStore = GlowstoneHacks.findEntityStore(GlowPlayer.class, "save");
                glowplayerStore.save(fakePlayer, tag);              //save player data to tag.
                GlowstoneHacks.writeCompressed(playerFile, tag);    //save tag to file.
            } catch (IOException e) {
                Rethrow.unchecked(e);
            }
        }, runnable -> scheduler.executeSyncPlayer(playerId, runnable, null));
    }

    //no need to call InventoryOpenEvent manually, GlowPlayer already does this for us! :D

}
