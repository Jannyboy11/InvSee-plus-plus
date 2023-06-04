package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.OpenResponse;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import com.janboerman.invsee.utils.Rethrow;
import net.glowstone.GlowServer;
import net.glowstone.entity.GlowEntity;
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
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

//TODO: similar implementation for InvSee++_Give

public class InvseeImpl implements InvseePlatform {

    static final ItemStack EMPTY_STACK = InventoryUtil.EMPTY_STACK;

    private final Plugin plugin;
    private final OpenSpectatorsCache cache;
    private final Scheduler scheduler;

    public InvseeImpl(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        GlowServer server = (GlowServer) plugin.getServer();
        GlowstoneHacks.injectWindowClickHandler(server);

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
    }

    //TODO implement! :D

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
        //TODO
        return null;
    }

    @Override
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory) {
        //TODO
        return null;
    }

    @Override
    public OpenResponse<MainSpectatorInventoryView> openMainSpectatorInventory(Player spectator, MainSpectatorInventory spectatorInventory, CreationOptions<PlayerInventorySlot> options) {
        //TODO
        return null;
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity target, CreationOptions<EnderChestSlot> options) {
        EnderInventory spectatorInv = new EnderInventory((GlowHumanEntity) target, options);
        cache.cache(spectatorInv);
        return spectatorInv;
    }

    @Override
    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, CreationOptions<EnderChestSlot> options) {
        //TODO
        return null;
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest) {
        //TODO
        return null;
    }

    @Override
    public OpenResponse<EnderSpectatorInventoryView> openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory spectatorInventory, CreationOptions<EnderChestSlot> options) {
        //TODO
        return null;
    }

    private <Slot, IS extends SpectatorInventory<Slot>> CompletableFuture<SpectateResponse<IS>> createOffline(UUID playerId, String playerName, CreationOptions<Slot> options, BiFunction<? super HumanEntity, ? super CreationOptions<Slot>, IS> invCreator) {
        GlowServer server = (GlowServer) plugin.getServer();
        NbtPlayerDataService playerDataService = (NbtPlayerDataService) server.getPlayerDataService();

        // Steps:
        //1. Create a fake player
        //2. Load data onto it
        //3. Return the inventory!

        //create the fake player
        Location location = server.getWorlds().get(0).getSpawnLocation();
        GlowPlayerProfile profile = new GlowPlayerProfile(playerName, playerId, true);
        GlowHumanEntity fakeHumanEntity = new FakeHumanEntity(location, profile);

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
                EntityStore glowhumanentityStore = GlowstoneHacks.findEntityStore(GlowHumanEntity.class, "load");
                glowhumanentityStore.load(fakeHumanEntity, tag);

                //return the inventory
                return SpectateResponse.succeed(invCreator.apply(fakeHumanEntity, options));
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
        GlowSession session = new GlowSession(server, gameServer.getProtocolProvider(), null/*netty channel*/, gameServer);
        GlowPlayerProfile profile = new GlowPlayerProfile(playerName, playerId, true);
        PlayerReader reader = playerDataService.beginReadingData(playerId);
        FakePlayer fakePlayer = new FakePlayer(session, profile, reader);

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

}
