package com.janboerman.invsee.spigot;

import static com.janboerman.invsee.utils.Compat.emptyList;
import static com.janboerman.invsee.utils.Compat.listCopy;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import com.janboerman.invsee.spigot.perworldinventory.PwiCommandArgs;
import com.janboerman.invsee.utils.StringHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class InvseeTabCompleter implements TabCompleter {

    private final InvseePlusPlus plugin;

    public InvseeTabCompleter(InvseePlusPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(InvseePlusPlus.TABCOMPLETION_PERMISSION)) return emptyList();

        InvseeAPI api = plugin.getApi();
        Player player = sender instanceof Player ? (Player) sender : null;

        if (args.length == 0) {
            Collection<? extends Player> onlinePlayers = sender.getServer().getOnlinePlayers();
            List<String> onlineNames = new ArrayList<>(onlinePlayers.size());
            if (player == null)
                for (Player onlinePlayer : onlinePlayers)
                    onlineNames.add(onlinePlayer.getName());
            else
                for (Player onlinePlayer : onlinePlayers)
                    if (player.canSee(onlinePlayer))
                        onlineNames.add(onlinePlayer.getName());

            if (plugin.offlinePlayerSupport() && plugin.offlinePlayerSupport()) {
                Set<String> offlineNames = api.getUuidCache().keySet();

                SortedSet<String> allNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                allNames.addAll(onlineNames);
                allNames.addAll(offlineNames);
                return listCopy(allNames);
            } else {
                onlineNames.sort(String.CASE_INSENSITIVE_ORDER);
                return onlineNames;
            }
        } else if (args.length == 1) {
            String prefix = args[0];

            Collection<? extends Player> onlinePlayers = sender.getServer().getOnlinePlayers();
            List<String> onlineNames = new ArrayList<>();
            if (player == null) {
                for (Player onlinePlayer : onlinePlayers) {
                    String onlineName = onlinePlayer.getName();
                    if (StringHelper.startsWithIgnoreCase(onlineName, prefix)) {
                        onlineNames.add(onlineName);
                    }
                }
            } else {
                for (Player onlinePlayer : onlinePlayers) {
                    String onlineName = onlinePlayer.getName();
                    if (player.canSee(onlinePlayer) && StringHelper.startsWithIgnoreCase(onlineName, prefix)) {
                        onlineNames.add(onlineName);
                    }
                }
            }

            if (plugin.offlinePlayerSupport() && plugin.tabCompleteOfflinePlayers()) {
                SortedSet<String> allNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                allNames.addAll(onlineNames);
                for (String offlineName : api.getUuidCache().keySet()) {
                    if (StringHelper.startsWithIgnoreCase(offlineName, prefix)) {
                        allNames.add(offlineName);
                    }
                }
                return listCopy(allNames);
            } else {
                onlineNames.sort(String.CASE_INSENSITIVE_ORDER);
                return onlineNames;
            }

        } else if (args.length == 2 && api instanceof PerWorldInventorySeeApi) {
            PerWorldInventorySeeApi pwiApi = (PerWorldInventorySeeApi) api;
            String pwiArgument = args[1];
            return PwiCommandArgs.complete(pwiArgument, pwiApi.getHook());
        }

        //TODO MultiVerse-Inventories

        return emptyList();
    }

}
