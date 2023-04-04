package com.janboerman.invsee.spigot.multiverseinventories;

import com.janboerman.invsee.utils.Either;
import com.janboerman.invsee.utils.Out;
import com.janboerman.invsee.utils.StringHelper;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import static com.onarandombox.multiverseinventories.profile.ProfileTypes.SURVIVAL;
import static com.onarandombox.multiverseinventories.profile.ProfileTypes.CREATIVE;
import static com.onarandombox.multiverseinventories.profile.ProfileTypes.ADVENTURE;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import org.bukkit.GameMode;
import org.bukkit.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MviCommandArgs {

    private static final String formError = "Expected the following from: MVI{<property>=<value>,...} where <property> is one of [group, world, gamemode].";

    private static final List<ProfileType> BUILTIN_PROFILE_TYPES = List.of(SURVIVAL, CREATIVE, ADVENTURE);

    String world;
    WorldGroup group;
    ProfileType profileType;

    ContainerType containerType;
    String containerName;

    private MviCommandArgs() {}

    public static Either<String, MviCommandArgs> parse(String argument, MultiverseInventoriesHook hook) {
        MviCommandArgs result = new MviCommandArgs();

        if (argument.isEmpty()) return Either.left(formError);
        if (!StringHelper.startsWithIgnoreCase(argument, "MVI")) return Either.left(formError);
        if (!StringHelper.startsWithIgnoreCase(argument, "MVI{") || !argument.endsWith("}")) return Either.left(formError);
        argument = argument.substring(4, argument.length() - 1);

        Optional<String> maybeError = parseProperties(result, argument, hook);
        if (maybeError.isPresent()) {
            return Either.left(maybeError.get());
        } else {
            return Either.right(result);
        }
    }

    private static Optional<String> parseProperties(@Out MviCommandArgs result, String propertyList, MultiverseInventoriesHook hook) {
        String[] properties = propertyList.split(",");
        for (String kv : properties) {
            String[] keyValue = kv.split("=", 2);
            if (keyValue.length != 2) return Optional.of("Invalid argument, expected <property>=<value>, but got " + kv + " instead.");
            String key = keyValue[0];
            String value = keyValue[1];

            if ("world".equalsIgnoreCase(key)) {
                result.world = value;
                result.containerName = result.world;
                result.containerType = ContainerType.WORLD;

            } else if ("group".equalsIgnoreCase(key)) {
                result.group = hook.getWorldGroupByName(value);
                if (result.group == null) return Optional.of("Invalid group: " + value + ", pick one of: " + hook.getWorldGroups().stream().map(WorldGroup::getName).collect(Collectors.toList()));

                result.containerName = result.group.getName();
                result.containerType = ContainerType.GROUP;

            } else if ("profile_type".equalsIgnoreCase(key)) {
                try {
                    result.profileType = ProfileTypes.forGameMode(GameMode.valueOf(value.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e1) {
                    try {
                        Method method = ProfileType.class.getDeclaredMethod("createProfileType", String.class);
                        method.setAccessible(true);
                        result.profileType = (ProfileType) method.invoke(null, value); //it is a static method
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e2) {
                        return Optional.of("Invalid gamemode: " + value + ", pick one of: " + Arrays.stream(GameMode.values())
                                .map(gm -> gm.name().toLowerCase(Locale.ROOT))
                                .collect(Collectors.joining(" ,", "[", "]")));
                    }
                }
            } else {
                return Optional.of("Invalid property, expected one of [group, world, profile_type] but got " + key + " instead.");
            }
        }

        if (result.world != null && result.group != null) {
            return Optional.of("Can't specify both group and world!");
        }

        return Optional.empty();
    }

    public static List<String> complete(final String argument, MultiverseInventoriesHook hook) {
        //TODO theadsafe-ify this!

        if (argument.length() < 4) return List.of("MVI{");
        if (!StringHelper.startsWithIgnoreCase(argument, "MVI{")) {
            return List.of("MVI{group=", "MVI{world=", "MVI{profile_type=");
        }

        String propertyList = argument.substring(4);
        if (propertyList.endsWith("}")) return List.of(argument);

        final Collection<String> groupNames = hook.getWorldGroups().stream().map(WorldGroup::getName).collect(Collectors.toList());
        final Collection<String> worldNames = hook.plugin.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toList());
        final Collection<String> profileTypeNames = BUILTIN_PROFILE_TYPES.stream().map(ProfileType::getName).collect(Collectors.toList());

        String[] properties = propertyList.split(",");
        if (properties.length == 0 || ((properties.length == 1 && properties[0].isEmpty()))) {
            List<String> ret = new ArrayList<>(9);
            for (String groupName : groupNames) ret.add("MVI{group=" + groupName + "}");
            for (String worldName : worldNames) ret.add("MVI{world=" + worldName + "}");
            for (String profileTypeName : profileTypeNames) ret.add("MVI{profile_type=" + profileTypeName + "}");
            return ret;
        }

        MviCommandArgs result = new MviCommandArgs();
        parseProperties(result, propertyList, hook); //ignore error message

        String lastProperty = properties[properties.length - 1];
        int stripLength = lastProperty.length();
        boolean endsWithComma = argument.endsWith(",");
        if (endsWithComma) stripLength += 1;
        String bufferBeforeLastProperty = argument.substring(0, argument.length() - stripLength);

        if (endsWithComma) {
            //we end with a comma, complete a new property
            List<String> everything = new ArrayList<>(9);
            if (result.group == null) {
                groupNames.stream().map(gn -> argument + "group=" + gn).forEach(everything::add);
            } else if (result.world == null) {
                worldNames.stream().map(wn -> argument + "world=" + wn).forEach(everything::add);
            } else if (result.profileType == null) {
                profileTypeNames.stream().map(pt -> argument + "profile_type" + pt).forEach(everything::add);
            } else {
                return List.of(argument.substring(argument.length() - 1) + "}");
            }
            return everything;
        }

        //we don't end with a comma - complete the property
        String[] propKeyValue = lastProperty.split("=", 2);
        if (propKeyValue.length == 0 || (propKeyValue.length == 1 && propKeyValue[0].isEmpty())) {
            if (result.group == null) {
                //group not yet specified, tabcomplete the group!
                return groupNames.stream().map(groupName -> bufferBeforeLastProperty + "group=" + groupName)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            } else if (result.world == null) {
                //world not yet specified, tabcomplete the world!
                return worldNames.stream().map(worldName -> bufferBeforeLastProperty + "world=" + worldName)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            } else if (result.profileType == null) {
                //profile_type not yet specified, tabcomplete the profile_type!
                return profileTypeNames.stream().map(profileTypeName -> bufferBeforeLastProperty + "profile_type=" + profileTypeName)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            } else {
                //everything specified!
                return List.of(argument + "}");
            }
        }

        String key = propKeyValue[0];
        if (propKeyValue.length == 1) {
            if (StringHelper.startsWithIgnoreCase("group", key)) {
                return List.of(bufferBeforeLastProperty + "group=");
            } else if (StringHelper.startsWithIgnoreCase("world", key)) {
                return List.of(bufferBeforeLastProperty + "world=");
            } else if (StringHelper.startsWithIgnoreCase("profile_type", key)) {
                return List.of(bufferBeforeLastProperty + "profile_typ");
            } else {
                return List.of(bufferBeforeLastProperty + "group=", bufferBeforeLastProperty + "world=", bufferBeforeLastProperty + "profile_type=");
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
            case "profile_type":
                Collection<String> matchingProfileTypes = profileTypeNames.stream().filter(pt -> StringHelper.startsWithIgnoreCase(pt, value)).collect(Collectors.toList());
                if (matchingProfileTypes.isEmpty()) matchingProfileTypes = profileTypeNames;
                return matchingProfileTypes.stream().map(pt -> bufferBeforeLastProperty + "profile_type=" + pt)
                        .flatMap(buf -> Stream.of(buf + "}", buf + ","))
                        .collect(Collectors.toList());
            default:
                //bogus property - just send every possible option
                Collection<String> everything = new ArrayList<>(9);
                if (result.group == null) everything.addAll(groupNames.stream().map(gn -> "group=" + gn).collect(Collectors.toList()));
                if (result.world == null) everything.addAll(worldNames.stream().map(wn -> "world=" + wn).collect(Collectors.toList()));
                if (result.profileType == null) everything.addAll(profileTypeNames.stream().map(pt -> "profile_type=" + pt).collect(Collectors.toList()));

                Stream<String> stream = everything.stream().map(property -> bufferBeforeLastProperty + property);
                if ((result.group != null || result.world != null) && result.profileType != null) {
                    stream = stream.map(buf -> buf + "}");
                } else {
                    stream = stream.flatMap(buf -> Stream.of(buf + "}", buf + ","));
                }
                return stream.collect(Collectors.toList());
        }
    }

}
