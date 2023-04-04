package com.janboerman.invsee.spigot.internal;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;

public class FakePersistentDataAdapterContext implements PersistentDataAdapterContext {
    @Override
    public PersistentDataContainer newPersistentDataContainer() {
        return new FakePersistentDataContainer(this);
    }
}
