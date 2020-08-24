package com.janboerman.invsee.spigot.perworldinventory;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FakePersistentDataContainer implements PersistentDataContainer {

    private final PersistentDataAdapterContext context = new FakePersistentDataAdapterContext();
    private final Map<NamespacedKey, Map<PersistentDataType, Object>> map = new HashMap<>();

    @Override
    public <T, Z> void set(@NotNull NamespacedKey namespacedKey, @NotNull PersistentDataType<T, Z> persistentDataType, @NotNull Z z) {
        map.computeIfAbsent(namespacedKey, k -> new HashMap<>()).put(persistentDataType, z);
    }

    @Override
    public <T, Z> boolean has(@NotNull NamespacedKey namespacedKey, @NotNull PersistentDataType<T, Z> persistentDataType) {
        var inner = map.get(namespacedKey);
        if (inner == null) return false;
        return inner.containsKey(persistentDataType);
    }

    @Nullable
    @Override
    public <T, Z> Z get(@NotNull NamespacedKey namespacedKey, @NotNull PersistentDataType<T, Z> persistentDataType) {
        var inner = map.get(namespacedKey);
        if (inner == null) return null;
        return (Z) inner.get(persistentDataType);
    }

    @NotNull
    @Override
    public <T, Z> Z getOrDefault(@NotNull NamespacedKey namespacedKey, @NotNull PersistentDataType<T, Z> persistentDataType, @NotNull Z z) {
        var inner = map.get(namespacedKey);
        if (inner == null) return z;
        if (inner.containsKey(persistentDataType)) {
            return (Z) inner.get(persistentDataType);
        } else {
            return z;
        }
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getKeys() {
        return map.keySet();
    }

    @Override
    public void remove(@NotNull NamespacedKey namespacedKey) {
        map.remove(namespacedKey);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @NotNull
    @Override
    public PersistentDataAdapterContext getAdapterContext() {
        return context;
    }
}
