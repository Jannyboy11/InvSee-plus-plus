package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.internal.ConstantTitle;

import java.util.function.Function;

@FunctionalInterface
public interface Title {

    public String titleFor(Target target);

    public static Title of(String title) {
        if (title == null) return null;
        return new ConstantTitle(title);
    }

    public static Title of(Function<? super Target, ? extends String> function) {
        if (function == null) return null;
        return function::apply;
    }

    public static Title defaultMainInventory() {
        return DefaultTitles.DEFAULT_MAIN_INVENTORY;
    }

    public static Title defaultEnderInventory() {
        return DefaultTitles.DEFAULT_ENDER_INVENTORY;
    }

}

class DefaultTitles {

    private DefaultTitles() {}

    static final Title DEFAULT_MAIN_INVENTORY = target -> target.toString() + "'s inventory";
    static final Title DEFAULT_ENDER_INVENTORY = target -> target.toString() + "'s enderchest";

}