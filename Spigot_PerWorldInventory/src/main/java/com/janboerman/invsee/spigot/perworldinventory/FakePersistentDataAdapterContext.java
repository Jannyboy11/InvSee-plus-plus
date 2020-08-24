package com.janboerman.invsee.spigot.perworldinventory;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class FakePersistentDataAdapterContext implements PersistentDataAdapterContext {
    @NotNull
    @Override
    public PersistentDataContainer newPersistentDataContainer() {
        return new FakePersistentDataContainer();
    }
}
