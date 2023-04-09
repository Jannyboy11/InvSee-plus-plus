package com.janboerman.invsee.spigot.api.resolve;

import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import com.janboerman.invsee.utils.CaseInsensitiveMap;
import com.janboerman.invsee.utils.UUIDHelper;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Strategy which resolves Unique IDs using BungeeCord's plugin messaging channel.
 *
 * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#uuidother">BungeeCord Plugin Messagin Channel</a> on the SpigotMC wiki
 */
public class UUIDBungeeCordStrategy implements UUIDResolveStrategy, PluginMessageListener {

    private static final String BUNGEECORD_CHANNEL = "BungeeCord";
    private static final String UUIDOther_SUBCHANNEL = "UUIDOther";

    private final Plugin plugin;

    private final Map<String, CompletableFuture<Optional<UUID>>> futureMap = Collections.synchronizedMap(new CaseInsensitiveMap<>());

    public UUIDBungeeCordStrategy(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);

        Server server = plugin.getServer();
        Messenger messenger = server.getMessenger();

        messenger.registerIncomingPluginChannel(plugin, BUNGEECORD_CHANNEL, this);
        messenger.registerOutgoingPluginChannel(plugin, BUNGEECORD_CHANNEL);
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
        Server server = plugin.getServer();
        Player player = null;
        for (Player p : server.getOnlinePlayers()) {
            player = p;
            break;
        }

        if (player == null) return CompletedEmpty.the();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeUTF(UUIDOther_SUBCHANNEL);
            dataOutputStream.writeUTF(userName);
            dataOutputStream.close();
            byte[] pluginMessage = byteArrayOutputStream.toByteArray();

            player.sendPluginMessage(plugin, BUNGEECORD_CHANNEL, pluginMessage);
            CompletableFuture<Optional<UUID>> future = new CompletableFuture<>();
            future.orTimeout(5, TimeUnit.SECONDS);
            future = future.exceptionally(throwable -> {
                if (!(throwable instanceof CancellationException || throwable instanceof TimeoutException)) {
                    plugin.getLogger().log(Level.WARNING, "Could not request " + userName + "'s UUID from BungeeCord", throwable);
                }
                futureMap.remove(userName);
                return Optional.empty();
            });
            futureMap.put(userName, future);
            return future;
        } catch (IOException e) {
            return CompletedEmpty.the();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (BUNGEECORD_CHANNEL.equals(channel)) {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(message));
            try {
                String subChannel = dataInputStream.readUTF();
                if (UUIDOther_SUBCHANNEL.equals(subChannel)) {
                    String userName = dataInputStream.readUTF();
                    String uuid = dataInputStream.readUTF();
                    CompletableFuture<Optional<UUID>> future = futureMap.remove(userName);
                    if (future != null) {
                        future.complete(Optional.of(UUIDHelper.dashed(uuid)));
                    }
                }
            } catch (IOException e) {
                //nothing we can do here - just let the future complete after the 5 second timeout.
            } finally {
                try {
                    dataInputStream.close();
                } catch (IOException e) {}
            }
        }
    }
}
