package com.janboerman.invsee.spigot.internal;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.UnknownServiceException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class FakePersistentDataContainer implements PersistentDataContainer {

    private final PersistentDataAdapterContext context;
    private final Map<NamespacedKey, Map<PersistentDataType, Object>> map = new HashMap<>();

    public FakePersistentDataContainer() {
        this(new FakePersistentDataAdapterContext());
    }

    FakePersistentDataContainer(PersistentDataAdapterContext context) {
        this.context = context;
    }

    @Override
    public <T, Z> void set(NamespacedKey namespacedKey, PersistentDataType<T, Z> persistentDataType, Z z) {
        map.computeIfAbsent(namespacedKey, k -> new HashMap<>()).put(persistentDataType, z);
    }

    @Override
    public <T, Z> boolean has(NamespacedKey namespacedKey, PersistentDataType<T, Z> persistentDataType) {
        Map<PersistentDataType, Object> inner = map.get(namespacedKey);
        if (inner == null) return false;
        return inner.containsKey(persistentDataType);
    }

    @Override
    public boolean has(@NotNull NamespacedKey namespacedKey) {
        return map.containsKey(namespacedKey);
    }

    @Override
    public <T, Z> Z get(NamespacedKey namespacedKey, PersistentDataType<T, Z> persistentDataType) {
        Map<PersistentDataType, Object> inner = map.get(namespacedKey);
        if (inner == null) return null;
        return (Z) inner.get(persistentDataType);
    }

    @Override
    public <T, Z> Z getOrDefault(NamespacedKey namespacedKey, PersistentDataType<T, Z> persistentDataType, Z z) {
        Map<PersistentDataType, Object> inner = map.get(namespacedKey);
        if (inner == null) return z;
        if (inner.containsKey(persistentDataType)) {
            return (Z) inner.get(persistentDataType);
        } else {
            return z;
        }
    }

    @Override
    public Set<NamespacedKey> getKeys() {
        return map.keySet();
    }

    @Override
    public void remove(NamespacedKey namespacedKey) {
        map.remove(namespacedKey);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public PersistentDataAdapterContext getAdapterContext() {
        return context;
    }

    @Override
    public int hashCode() {
        return 3 + map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof FakePersistentDataContainer) {
            //Use pattern matching if I ever upgrade to Java 17
            FakePersistentDataContainer fakeThat = (FakePersistentDataContainer) obj;
            return Objects.equals(this.map, fakeThat.map);
        }
        else if (obj instanceof PersistentDataContainer) {
            PersistentDataContainer that = (PersistentDataContainer) obj;

            if (!Objects.equals(this.getKeys(), that.getKeys())) return false;
            for (NamespacedKey key : getKeys()) {
                Map<PersistentDataType, Object> innerMap = map.get(key);

                for (Entry<PersistentDataType, Object> entry : innerMap.entrySet()) {
                    PersistentDataType pdt = entry.getKey();
                    Object myValue = entry.getValue();

                    Object otherValue = that.get(key, pdt);
                    if (!Objects.equals(myValue, otherValue)) return false;
                }
            }

            return true;
        }
        else {
            //includes obj == null
            return false;
        }
    }

    @Override
    public byte @NotNull [] serializeToBytes() throws IOException {
        throw new UnknownServiceException("Can't serialize fake data");
    }

    @Override
    public void readFromBytes(byte @NotNull [] bytes, boolean b) throws IOException {
        throw new UnknownServiceException("Can't deserialize fake data");
    }

}
