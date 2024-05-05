package com.janboerman.invsee.spigot.addon.give.cmd;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.janboerman.invsee.spigot.addon.give.common.Convert;
import com.janboerman.invsee.spigot.addon.give.common.GiveApi;
import com.janboerman.invsee.spigot.addon.give.common.ItemType;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.utils.Either;
import com.janboerman.invsee.utils.ListHelper;
import com.janboerman.invsee.utils.Pair;

import org.bukkit.inventory.ItemStack;

public final class ArgParser {

    private static final List<ArgType>
            FORMAT_ITEMTYPE_AMOUNT = ListHelper.of(ArgType.TARGET, ArgType.ITEM_TYPE, ArgType.AMOUNT),
            FORMAT_ITEMTYPE_AMOUNT_NBTTAG = ListHelper.of(ArgType.TARGET, ArgType.ITEM_TYPE, ArgType.AMOUNT, ArgType.NBT_TAG),
            FORMAT_ITEMTYPE = ListHelper.of(ArgType.TARGET, ArgType.ITEM_TYPE);

    // item type strategy:
    //  If the args length is at least 3 and last argument is a number:
    //      assume the format <target> <item type> <amount>
    //  If the last argument is not a number:
    //      If the last argument ends in a '}' character:
    //          assume the old format: <target> <item type> <amount> <nbt tag>
    //      Else (so it is 3 or less):
    //          interpret according to format <target> <item type>
    //  Where
    //      <item type> is either:
    //      | <material>
    //      | <material>:<data value>
    //      | <vanilla item type>[components]?

    private ArgParser() {
    }

    public static Optional<List<ArgType>> determineFormat(String[] arguments) {
        if (arguments.length < 2) {
            return Optional.empty(); // caller returns false, bukkit will respond with the usage string.
        }

        List<ArgType> format;

        String lastArg = arguments[arguments.length - 1];

        try {
            Integer.parseInt(lastArg);
            format = FORMAT_ITEMTYPE_AMOUNT;
        } catch (NumberFormatException e) {
            if (lastArg.endsWith("}")) {
                format = FORMAT_ITEMTYPE_AMOUNT_NBTTAG;
            } else {
                format = FORMAT_ITEMTYPE;
            }
        }

        return Optional.of(format);
    }

    public static Map<ArgType, String> splitArguments(List<ArgType> format, String[] args) {
        Map<ArgType, String> result = new EnumMap<>(ArgType.class);
        result.put(ArgType.TARGET, args[0]);
        if (FORMAT_ITEMTYPE.equals(format)) {
            result.put(ArgType.ITEM_TYPE, args[1]);
        } else if (FORMAT_ITEMTYPE_AMOUNT.equals(format)) {
            String last = args[args.length - 1];
            List<String> init = ListHelper.of(args).subList(0, args.length - 1);
            result.put(ArgType.AMOUNT, last);
            result.put(ArgType.ITEM_TYPE, String.join(" ", init));
        } else if (FORMAT_ITEMTYPE_AMOUNT_NBTTAG.equals(format)) {
            result.put(ArgType.ITEM_TYPE, args[1]);
            result.put(ArgType.AMOUNT, args[2]);
            result.put(ArgType.NBT_TAG, String.join(" ", Arrays.asList(args).subList(3, args.length)));
        } else {
            throw new IllegalArgumentException("Unexpected format: " + format);
        }

        return result;
    }

    public static Pair<CompletableFuture<Optional<UUID>>, CompletableFuture<Optional<String>>> parseTarget(InvseeAPI invseeApi, String target) {
        Either<UUID, String> eitherPlayer = Convert.convertPlayer(target);
        CompletableFuture<Optional<UUID>> uuidFuture;
        CompletableFuture<Optional<String>> userNameFuture;
        if (eitherPlayer.isLeft()) {
            UUID uuid = eitherPlayer.getLeft();
            uuidFuture = CompletableFuture.completedFuture(Optional.of(uuid));
            userNameFuture = invseeApi.fetchUserName(uuid);
        } else {
            assert eitherPlayer.isRight();
            String userName = eitherPlayer.getRight();
            userNameFuture = CompletableFuture.completedFuture(Optional.of(userName));
            uuidFuture = invseeApi.fetchUniqueId(userName);
        }
        return new Pair<>(uuidFuture, userNameFuture);
    }

    public static Either<String, ItemStack> parseItem(GiveApi giveApi, Map<ArgType, String> arguments) {
        try {
            return getAmount(arguments)
                    .flatMap(amount -> getItemType(arguments, giveApi).flatMap(itemType -> itemType.toItemStack(amount)))
                    .flatMap(itemStack -> applyTag(arguments, giveApi, itemStack));
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private static Either<String, Integer> getAmount(Map<ArgType, String> arguments) {
        String amountString = arguments.get(ArgType.AMOUNT);
        if (amountString == null) {
            return Either.right(1);
        } else {
            return Convert.convertAmount(amountString);
        }
    }

    private static Either<String, ItemType> getItemType(Map<ArgType, String> arguments, GiveApi giveApi) {
        return giveApi.parseItemType(arguments.get(ArgType.ITEM_TYPE));
    }

    private static Either<String, ItemStack> applyTag(Map<ArgType, String> arguments, GiveApi giveApi, ItemStack stack) {
        return Either.right(giveApi.applyTag(stack, arguments.get(ArgType.NBT_TAG)));
    }
}
