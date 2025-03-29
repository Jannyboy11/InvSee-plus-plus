package com.janboerman.invsee.paper;

import static com.janboerman.invsee.utils.Compat.listOf;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import com.janboerman.invsee.spigot.perworldinventory.PwiCommandArgs;
import com.janboerman.invsee.utils.StringHelper;
import com.janboerman.invsee.utils.UsernameTrie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class AsyncTabCompleter implements Listener {

    private final Set<String> knownLabels;
    private final InvseeAPI api;

    private final UsernameTrie<Void> knownPlayerNames = new UsernameTrie<>();
    private final ConcurrentLinkedQueue<String> nameQueue = new ConcurrentLinkedQueue<>();

    public AsyncTabCompleter(InvseePlusPlus plugin, Scheduler scheduler, OfflinePlayerProvider playerDatabase) {
        this.api = plugin.getApi();

        this.knownLabels = new ConcurrentSkipListSet<>(String.CASE_INSENSITIVE_ORDER);
        this.knownLabels.addAll(listOf("invsee", "inventorysee", "isee", "endersee", "enderchestsee", "esee"));

        String pluginNameLower = "invseeplusplus";
        List<String> withPrefix = this.knownLabels.stream().map(s -> pluginNameLower + ":" + s).collect(Collectors.toList());
        this.knownLabels.addAll(withPrefix);

        scheduler.executeAsync(() -> playerDatabase.getAll(this::enqueue));
    }

    private void enqueue(String name) {
        if (name != null) {
            nameQueue.add(name);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        enqueue(event.getPlayer().getName());
    }

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent event) {
        // Can be called from multiple threads. By the looks of it these are Netty threads.
        //if we can adjust the UsernameTrie implementation such that lookups done for 'get' and 'tabcompletion' don't restructure the internal tree structure,
        //then we could use a ReentrantReadWriteLock to protect the UsernameTrie.
        //alternatively, we could just make the UsernameTrie implementation itself thread-safe?
        //but I don't think it's such a big problem right now - incorrect tabcompletions are not the end of the world.
        // As of InvSee++ v0.19.19, the UsernameTrie implementation itself was made thread-safe

        final String buffer = event.getBuffer();

        while (!nameQueue.isEmpty()) {
            String name = nameQueue.poll();
            if (name != null) {
                knownPlayerNames.insert(name, null);
            }
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
