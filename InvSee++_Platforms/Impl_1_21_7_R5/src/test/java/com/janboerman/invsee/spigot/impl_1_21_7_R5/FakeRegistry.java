package com.janboerman.invsee.spigot.impl_1_21_7_R5;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

public class FakeRegistry<T extends Keyed> implements Registry<T> {

    private final Map<NamespacedKey, T> map;

    FakeRegistry(Map<NamespacedKey, T> map) {
        this.map = map;
    }

    @Override
    public T get(NamespacedKey namespacedKey) {
        return map.get(namespacedKey);
    }

    @Override
    public T getOrThrow(NamespacedKey namespacedKey) {
        if (map.containsKey(namespacedKey)) {
            return map.get(namespacedKey);
        } else {
            throw new NoSuchElementException("No value found for NamespacedKey " + namespacedKey);
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
