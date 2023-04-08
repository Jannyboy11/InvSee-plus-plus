package com.janboerman.invsee.spigot.impl_1_19_3_R2;

import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftHumanEntity;

public class FakeCraftHumanEntity extends CraftHumanEntity {

    public FakeCraftHumanEntity(CraftServer server, FakeEntityHuman entity) {
        super(server, entity);
    }

    @Override
    public FakeEntityHuman getHandle() {
        return (FakeEntityHuman) this.entity;
    }

}
