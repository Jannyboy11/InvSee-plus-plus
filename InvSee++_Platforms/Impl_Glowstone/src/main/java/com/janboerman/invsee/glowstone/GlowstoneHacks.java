package com.janboerman.invsee.glowstone;

import com.flowpowered.network.MessageHandler;
import com.flowpowered.network.service.HandlerLookupService;
import net.glowstone.GlowServer;
import net.glowstone.entity.GlowEntity;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.inventory.GlowInventory;
import net.glowstone.inventory.GlowInventorySlot;
import net.glowstone.io.entity.EntityStore;
import net.glowstone.io.nbt.NbtPlayerDataService;
import net.glowstone.net.GlowSession;
import net.glowstone.net.message.play.inv.WindowClickMessage;
import net.glowstone.net.protocol.GlowProtocol;
import net.glowstone.net.protocol.PlayProtocol;
import net.glowstone.util.nbt.CompoundTag;
import net.glowstone.util.nbt.NbtInputStream;
import net.glowstone.util.nbt.NbtOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

final class GlowstoneHacks {

    static List<GlowInventorySlot> getSlots(GlowInventory inventory) {
        try {
            Field field = GlowInventory.class.getDeclaredField("slots");
            field.setAccessible(true);
            Object slots = field.get(inventory);
            return (List<GlowInventorySlot>) slots;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get GlowInventory slots reflectively.", e);
        }
    }

    static void setSlots(GlowInventory inventory, List<GlowInventorySlot> slots) {
        try {
            Field field = GlowInventory.class.getDeclaredField("slots");
            field.setAccessible(true);
            field.set(inventory, slots);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set GlowInventory slots reflectively.", e);
        }
    }

    static void injectWindowClickHandler(GlowServer server) {
        HandlerLookupService playHandlers = getHandlers(getPlayProtocol(server));

        MessageHandler<GlowSession, WindowClickMessage> oldHandler = (MessageHandler) playHandlers.find(WindowClickMessage.class);
        if (oldHandler != null && !(oldHandler instanceof DecoratedWindowClickHandler)) { //idempotency!
            var newHandler = new DecoratedWindowClickHandler(oldHandler);
            try {
                playHandlers.bind(WindowClickMessage.class, newHandler);
            } catch (InstantiationException | IllegalAccessException neverThrown) {
                throw new RuntimeException("Impossible", neverThrown);
            }
        }
    }

    private static PlayProtocol getPlayProtocol(GlowServer server) {
        return server.getNetworkServer().getProtocolProvider().getPlay();
    }

    private static HandlerLookupService getHandlers(GlowProtocol protocol) {
        try {
            Field field = GlowProtocol.class.getDeclaredField("handlers");
            field.setAccessible(true);
            return (HandlerLookupService) field.get(protocol);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get GlowProtocol handlers reflectively.", e);
        }
    }

    static File getPlayerDir(NbtPlayerDataService playerDataService) {
        try {
            Field field = NbtPlayerDataService.class.getField("playerDir");
            field.setAccessible(true);
            return (File) field.get(playerDataService);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get player data folder reflectively.", e);
        }
    }

    static CompoundTag readCompressed(File playerFile) throws IOException {
        try (NbtInputStream in = new NbtInputStream(new FileInputStream(playerFile))) {
            return in.readCompound();
        }
    }

    static void writeCompressed(File playerFile, CompoundTag tag) throws IOException {
        try (NbtOutputStream out = new NbtOutputStream(new FileOutputStream(playerFile))) {
            out.writeTag(tag);
        }
    }

    static EntityStore<? extends GlowEntity> findEntityStore(Class<? extends GlowEntity> clazz, String type) {
        try {
            Method method = EntityStore.class.getDeclaredMethod("find", Class.class, String.class);
            method.setAccessible(true);
            return (EntityStore<? extends GlowEntity>) method.invoke(null, clazz, type);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to find GlowPlayer entity store.", e);
        }
    }
}
