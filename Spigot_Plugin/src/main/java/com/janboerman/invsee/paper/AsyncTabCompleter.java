package com.janboerman.invsee.paper;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import com.janboerman.invsee.spigot.perworldinventory.PwiCommandArgs;
import com.janboerman.invsee.utils.StringHelper;
import com.janboerman.invsee.utils.UsernameTrie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class AsyncTabCompleter implements Listener {

    private final InvseePlusPlus plugin;
    private final Set<String> knownLabels;
    private final InvseeAPI api;

    private final UsernameTrie<Void> knownPlayerNames = new UsernameTrie<>();
    private final ConcurrentLinkedQueue<String> nameQueue = new ConcurrentLinkedQueue<>();

    public AsyncTabCompleter(InvseePlusPlus plugin) {
        this.plugin = plugin;
        this.api = plugin.getApi();

        this.knownLabels = new ConcurrentSkipListSet<>(String.CASE_INSENSITIVE_ORDER);
        this.knownLabels.addAll(List.of("invsee", "inventorysee", "isee", "endersee", "enderchestsee", "esee"));

        String pluginNameLower = "invseeplusplus";
        List<String> withPrefix = this.knownLabels.stream().map(s -> pluginNameLower + ":" + s).collect(Collectors.toList());
        this.knownLabels.addAll(withPrefix);

        final OfflinePlayerProvider provider = plugin.getOfflinePlayerProvider();
        final BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> provider.getAll(nameQueue::add));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        nameQueue.add(event.getPlayer().getName());
    }

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent event) {
        // Can be called from multiple threads. By the looks of it these are Netty threads.
        //TODO if we can adjust the UsernameTrie implementation such that lookups done for 'get' and 'tabcompletion' don't restructure the internal tree structure,
        //TODO then we could use a ReentrantReadWriteLock to protect the UsernameTrie.
        //TODO alternatively, we could just make the UsernameTrie implementation itself thread-safe?
        //TODO but I don't think it's such a big problem right now - incorrect tabcompletions are not the end of the world.

        final String buffer = event.getBuffer();

        while (!nameQueue.isEmpty()) {
            knownPlayerNames.insert(nameQueue.poll(), null);
        }

        if (event.isCommand()) {
            String matchedLabel = null;
            boolean equalsIgnoreCase = false;
            boolean prefixIgnoreCase = false;
            for (String label : knownLabels) {
                if (buffer.equalsIgnoreCase("/" + label + " ")) {
                    matchedLabel = label;
                    equalsIgnoreCase = true;
                    break;
                } else if (StringHelper.startsWithIgnoreCase(buffer, "/" + label + " ")) {
                    matchedLabel = label;
                    prefixIgnoreCase = true;
                    break;
                }
            }

            if (equalsIgnoreCase) {
                List<String> playerNames = new ArrayList<>();
                knownPlayerNames.traverse("", (name, v) -> playerNames.add(name));
                if (!playerNames.isEmpty()) {
                    event.setCompletions(playerNames);
                    event.setHandled(true);
                }
            } else if (prefixIgnoreCase) {
                String[] split = buffer.split("\\s");
                if (split.length == 2) {
                    List<String> playerNames = new ArrayList<>();
                    String prefix = buffer.substring(matchedLabel.length() + 2);
                    knownPlayerNames.traverse(prefix, (name, v) -> playerNames.add(name));
                    if (!playerNames.isEmpty()) {
                        event.setCompletions(playerNames);
                        event.setHandled(true);
                    }
                } else if (split.length == 3 && api instanceof PerWorldInventorySeeApi) {
                    PerWorldInventorySeeApi pwiApi = (PerWorldInventorySeeApi) api;
                    String pwiArgument = split[2];
                    List<String> pwiCompletions = PwiCommandArgs.complete(pwiArgument, pwiApi.getHook());
                    event.setCompletions(pwiCompletions);
                    event.setHandled(true);
                }
            }
        }
    }

}
