package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.internal.ConstantTitle;

import java.util.function.Function;

/**
 * Represents the title of a {@link SpectatorInventoryView}.
 * The title string is computed from a {@link Target}.
 */
@FunctionalInterface
public interface Title {

    /**
     * Get the title string for a given {@linkplain Target}.
     * @param target the target
     * @return the title string
     */
    public String titleFor(Target target);

    /**
     * Create a title with a constant tile string.
     * @param title the constant title string
     * @return the Title
     */
    public static Title of(String title) {
        if (title == null) return null;
        return new ConstantTitle(title);
    }

    /** Convert a Function into a Title. */
    public static Title of(Function<? super Target, ? extends String> function) {
        if (function == null) return null;
        return function::apply;
    }

    /** Get the default main spectator inventory title: {@code "<target>'s inventory"} */
    public static Title defaultMainInventory() {
        return DefaultTitles.DEFAULT_MAIN_INVENTORY;
    }

    /** Get the default ender chest spectator inventory title: {@code "<target>'s enderchest"} */
    public static Title defaultEnderInventory() {
        return DefaultTitles.DEFAULT_ENDER_INVENTORY;
    }

}

class DefaultTitles {

    private DefaultTitles() {}

    static final Title DEFAULT_MAIN_INVENTORY = target -> target.toString() + "'s inventory";
    static final Title DEFAULT_ENDER_INVENTORY = target -> target.toString() + "'s enderchest";

}