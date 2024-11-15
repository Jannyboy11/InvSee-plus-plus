package com.janboerman.invsee.spigot.impl_1_21_1_R1;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

public class FakeRegistry<T extends Keyed> implements Registry<T> {

    private final Map<NamespacedKey, T> map;

    FakeRegistry(Map<NamespacedKey, T> map) {
        this.map = map;
    }

    @Override
    public T get(NamespacedKey namespacedKey) {
        return map.get(namespacedKey);
    }

    @NotNull
    @Override
    public T getOrThrow(@NotNull NamespacedKey namespacedKey) {
        T item = map.get(namespacedKey);
        if (item == null) {
            throw new NoSuchElementException(namespacedKey.toString());
        } else {
            return item;
        }
    }

    @Override
    public Stream<T> stream() {
        return map.values().stream();
    }

    @Override
    public Iterator<T> iterator() {
        return map.values().iterator();
    }
}
