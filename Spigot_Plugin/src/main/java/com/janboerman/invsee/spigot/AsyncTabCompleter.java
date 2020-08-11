package com.janboerman.invsee.spigot;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.utils.StringHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class AsyncTabCompleter implements Listener {

    private final InvseePlusPlus plugin;
    private final OfflinePlayerProvider provider;
    private final Set<String> knownLabels;

    public AsyncTabCompleter(InvseePlusPlus plugin) {
        this.plugin = plugin;
        this.provider = plugin.getOfflinePlayerProvider();

        this.knownLabels = new ConcurrentSkipListSet<>(String.CASE_INSENSITIVE_ORDER);
        this.knownLabels.addAll(List.of("invsee", "inventorysee", "isee", "endersee", "enderchestsee", "esee"));

        String pluginNameLower = "invseeplusplus";
        List<String> withPrefix = this.knownLabels.stream().map(s -> pluginNameLower + ":" + s).collect(Collectors.toList());
        this.knownLabels.addAll(withPrefix);
    }

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent event) {
        String buffer = event.getBuffer();

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
                }
            }

            Set<String> playerNames = Set.of();
            if (equalsIgnoreCase) {
                playerNames = provider.getAll();
            } else if (prefixIgnoreCase) {
                playerNames = provider.getWithPrefix(buffer.substring(matchedLabel.length() + 2));
            }

            if (!playerNames.isEmpty()) {
                event.setCompletions(List.copyOf(playerNames));
                event.setHandled(true);
            }
        }
    }

}
