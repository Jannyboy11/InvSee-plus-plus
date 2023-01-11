package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface Title {

    public String titleFor(Target target);

    public static Title constant(String title) {
        if (title == null) return null;
        return new ConstantTitle(title);
    }

    public static Title of(Function<? super Target, ? extends String> function) {
        if (function == null) return null;
        return function::apply;
    }

}

class ConstantTitle implements Title {

    private String title;

    ConstantTitle(String title) {
        this.title = Objects.requireNonNull(title);
    }

    @Override
    public final String titleFor(Target target) {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ConstantTitle)) return false;

        ConstantTitle that = (ConstantTitle) o;
        return this.title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }
}