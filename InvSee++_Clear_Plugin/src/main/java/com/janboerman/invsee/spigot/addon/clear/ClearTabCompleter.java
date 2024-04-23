package com.janboerman.invsee.spigot.addon.clear;

import static com.janboerman.invsee.utils.Compat.emptyList;

import com.janboerman.invsee.utils.StringHelper;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class ClearTabCompleter implements TabCompleter {

    ClearTabCompleter() {}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0 || args[0].isEmpty()) return null; //tabcomplete players

        else if (args.length == 1) {
            final String inputName = args[0];
            Stream<? extends Player> targets = sender.getServer().getOnlinePlayers().stream();
            if (sender instanceof Player) {
                Player player = (Player) sender;
                targets = targets.filter(player::canSee);
            }
            return targets.map(Player::getName)
                    .filter(name -> StringHelper.startsWithIgnoreCase(name, inputName))
                    .collect(Collectors.toList());
        }

        else if (args.length == 2) {
            final String inputMaterial = args[1].toLowerCase();
            Stream<String> materialNames = Arrays.stream(Material.values()).map(material -> material.name().toLowerCase());
            if (!inputMaterial.isEmpty()) {
                materialNames = materialNames.filter(name -> name.startsWith(inputMaterial));
            }
            return materialNames.collect(Collectors.toList());
        }

        else if (args.length == 3) {
            final String inputAmount = args[2];
            Material material = Material.matchMaterial(args[1]);
            Stream<String> amounts = IntStream.rangeClosed(1, material != null ? material.getMaxStackSize() : 64)
                    .mapToObj(Integer::toString);
            if (!inputAmount.isEmpty()) {
                amounts = amounts.filter(amount -> amount.startsWith(inputAmount));
            }
            return amounts.collect(Collectors.toList());
        }

        return emptyList();
    }
}
