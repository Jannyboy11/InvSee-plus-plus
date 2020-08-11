package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.utils.StringHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvseeTabCompleter implements TabCompleter {

    private final InvseePlusPlus plugin;

    public InvseeTabCompleter(InvseePlusPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 0) {
            InvseeAPI api = plugin.getApi();
            Set<String> cached = api.getUuidCache().keySet();
            Set<String> online = sender.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));
            online.addAll(cached);
            return List.copyOf(online);
        } else if (args.length == 1) {
            String prefix = args[0];
            InvseeAPI api = plugin.getApi();
            Set<String> cached = api.getUuidCache().keySet();
            return Stream.concat(sender.getServer().getOnlinePlayers().stream().map(Player::getName), cached.stream())
                    .filter(name -> StringHelper.startsWithIgnoreCase(name, prefix))
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
