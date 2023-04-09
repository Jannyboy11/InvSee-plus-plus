package com.janboerman.invsee.spigot.internal;

import com.janboerman.invsee.spigot.api.Title;
import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Objects;

public class ConstantTitle implements Title {

    private String title;

    public ConstantTitle(String title) {
        this.title = Objects.requireNonNull(title);
    }

    public String getTitle() {
        return title;
    }

    @Override
    public final String titleFor(Target target) {
        return getTitle();
    }

    @Override
    public String toString() {
        return getTitle();
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
