package com.janboerman.invsee.spigot.perworldinventory;

import com.janboerman.invsee.utils.Either;
import com.janboerman.invsee.utils.StringHelper;
import me.ebonjaeger.perworldinventory.Group;
import me.ebonjaeger.perworldinventory.data.ProfileKey;
import org.bukkit.GameMode;
import org.bukkit.World;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PwiCommandArgs {

    private static final String formError = "Expected the following from: PWI{<property>=<value>,...} where <property> is one of [group, world, gamemode].";

    private String world;
    private Group group;
    private GameMode gameMode;

    private PwiCommandArgs() {}

    public static Either<String, PwiCommandArgs> parse(String argument, PerWorldInventoryHook hook) {
        PwiCommandArgs result = new PwiCommandArgs();

        if (argument.isEmpty()) return Either.left(formError);
        if (!StringHelper.startsWithIgnoreCase(argument, "PWI")) return Either.left(formError);
        if (!StringHelper.startsWithIgnoreCase(argument, "PWI{") || !argument.endsWith("}")) return Either.left(formError);
        argument = argument.substring(4, argument.length() - 1);

        Optional<String> maybeError = parseProperties(result, argument, hook);
        if (maybeError.isPresent()) {
            return Either.left(maybeError.get());
        } else {
            return Either.right(result);
        }
    }

    private static Optional<String> parseProperties(PwiCommandArgs result, String propertyList, PerWorldInventoryHook hook) {
        String[] properties = propertyList.split(",");
        for (String kv : properties) {
            String[] keyValue = kv.split("=", 2);
            if (keyValue.length != 2) return Optional.of("Invalid argument, expected <property>=<value>, but got " + kv + " instead.");
            String key = keyValue[0];
            String value = keyValue[1];

            if ("world".equalsIgnoreCase(key)) {
                result.world = value;

            } else if ("group".equalsIgnoreCase(key)) {
                result.group = hook.getGroupByName(value);
                if (result.group == null) return Optional.of("Invalid group: " + value + ", pick one of: " + hook.getGroupManager().getGroups().keySet());

            } else if ("gamemode".equalsIgnoreCase(key)) {
                try {
                    result.gameMode = GameMode.valueOf(value.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    return Optional.of("Invalid gamemode: " + value + ", pick one of: " + Arrays.stream(GameMode.values())
                            .map(gm -> gm.name().toLowerCase(Locale.ROOT))
                            .collect(Collectors.joining(" ,", "[", "]")));
                }

            } else {
                return Optional.of("Invalid property, expected one of [group, world, gamemode] but got " + key + "instead.");
            }
        }

        return Optional.empty();
    }

    public static List<String> complete(final String argument, PerWorldInventoryHook hook) {
        if (argument.length() < 4) return List.of("PWI{");
        if (!StringHelper.startsWithIgnoreCase(argument, "PWI{")) {
            return List.of("PWI{group=", "PWI{world=", "PWI{gamemode=");
        }

        String propertyList = argument.substring(4);
        if (propertyList.endsWith("}")) return List.of(argument);

        final Collection<String> groupNames = hook.getGroupManager().getGroups().keySet();
        final Collection<String> worldNames = hook.plugin.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toList());
        final Collection<String> gameModes = Arrays.stream(GameMode.values()).map(gm -> gm.name().toLowerCase(Locale.ROOT)).collect(Collectors.toList());

        String[] properties = propertyList.split(",");
        if (properties.length == 0 || ((properties.length == 1 && properties[0].isEmpty()))) {
            List<String> ret = new ArrayList<>(9);
            for (String groupName : groupNames) {
                ret.add("PWI{group=" + groupName + "}");
            }
            for (String worldName : worldNames) {
                ret.add("PWI{world=" + worldName + "}");
            }
            for (String gameMode : gameModes) {
                ret.add("PWI{gamemode=" + gameMode + "}");
            }
            return ret;
        }

        PwiCommandArgs result = new PwiCommandArgs();
        parseProperties(result, propertyList, hook); //ignore errormessage

        String lastProperty = properties[properties.length - 1];
        String bufferBeforeLastProperty = argument.substring(0, argument.length() - lastProperty.length());
        //TODO fix: I think bufferBeforeLastProperty can contain the trailing comma.

        String[] propKeyValue = lastProperty.split("=", 2);
        if (propKeyValue.length == 0 || (propKeyValue.length == 1 && propKeyValue[0].isEmpty())) {
            if (result.group == null) {
                //tab the group
                return groupNames.stream().map(groupName -> bufferBeforeLastProperty + "group=" + groupName)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            } else if (result.world == null) {
                //tab the world
                return worldNames.stream().map(worldName -> bufferBeforeLastProperty + "world=" + worldName)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            } else if (result.gameMode == null) {
                //tab the gamemode
                return gameModes.stream().map(gameMode -> bufferBeforeLastProperty + "gamemode=" + gameMode)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            } else {
                //all properties are specified - we are done!
                return List.of(argument + "}");
            }
        }

        String key = propKeyValue[0];
        if (propKeyValue.length == 1) {
            if (StringHelper.startsWithIgnoreCase("group", key)) {
                return List.of(bufferBeforeLastProperty + "group=");
            } else if (StringHelper.startsWithIgnoreCase("world", key)) {
                return List.of(bufferBeforeLastProperty + "world=");
            } else if (StringHelper.startsWithIgnoreCase("gamemode", key)) {
                return List.of(bufferBeforeLastProperty + "gamemode=");
            } else {
                return List.of(bufferBeforeLastProperty + "group=", bufferBeforeLastProperty + "world=", bufferBeforeLastProperty + "gamemode=");
            }
        }

        String value = propKeyValue[1];
        switch (key.toLowerCase(Locale.ROOT)) {
            case "group":
                Collection<String> matchingGroups = groupNames.stream().filter(gn -> StringHelper.startsWithIgnoreCase(gn, value)).collect(Collectors.toList());
                if (matchingGroups.isEmpty()) matchingGroups = groupNames;
                return matchingGroups.stream().map(gn -> bufferBeforeLastProperty + "group=" + gn)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            case "world":
                Collection<String> matchingWorlds = worldNames.stream().filter(wn -> StringHelper.startsWithIgnoreCase(wn, value)).collect(Collectors.toList());
                if (matchingWorlds.isEmpty()) matchingWorlds = worldNames;
                return matchingWorlds.stream().map(wn -> bufferBeforeLastProperty + "world=" + wn)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            case "gamemode":
                Collection<String> matchingGameModes = gameModes.stream().filter(gm -> StringHelper.startsWithIgnoreCase(gm, value)).collect(Collectors.toList());
                if (matchingGameModes.isEmpty()) matchingGameModes = gameModes;
                return matchingGameModes.stream().map(gm -> bufferBeforeLastProperty + "gamemode=" + gm)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            default:
                //bogus property - just send every possible option.
                Collection<String> everything = new ArrayList<>(9);
                if (result.group == null) everything.addAll(groupNames.stream().map(gn -> "group=" + gn).collect(Collectors.toList()));
                if (result.world == null) everything.addAll(worldNames.stream().map(wn -> "world=" + wn).collect(Collectors.toList()));
                if (result.gameMode == null) everything.addAll(gameModes.stream().map(gm -> "gamemode=" + gm).collect(Collectors.toList()));

                Stream<String> stream = everything.stream().map(property -> bufferBeforeLastProperty + property);
                if (result.group != null && result.world != null && result.gameMode != null) {
                    stream = stream.map(buf -> buf + "}");
                } else {
                    stream = stream.flatMap(buf -> Stream.of(buf + "}", buf + ","));
                }
                return stream.collect(Collectors.toList());
        }
    }

    public static ProfileKey toProfileKey(UUID playerId, PwiCommandArgs options, PerWorldInventoryHook hook) {
        Group group;
        GameMode gameMode = options.gameMode;
        if (gameMode == null) gameMode = GameMode.SURVIVAL;

        if (options.group != null) {
            group = options.group;
        } else if (options.world != null) {
            group = hook.getGroupForWorld(options.world);
        } else {
            //unmanaged group!
            group = new Group("", Set.of(), gameMode, null);
        }

        if (options.world != null) {
            group.addWorld(options.world);
        }

        return new ProfileKey(playerId, group, gameMode);
    }
}
