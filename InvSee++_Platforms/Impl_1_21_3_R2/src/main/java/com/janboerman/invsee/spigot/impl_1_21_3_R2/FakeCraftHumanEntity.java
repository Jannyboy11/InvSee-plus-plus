package com.janboerman.invsee.spigot.impl_1_21_3_R2;

import org.bukkit.craftbukkit.v1_21_R2.CraftServer;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftHumanEntity;

public class FakeCraftHumanEntity extends CraftHumanEntity {

    public FakeCraftHumanEntity(CraftServer server, FakeEntityHuman entity) {
        super(server, entity);
    }

    @Override
    public FakeEntityHuman getHandle() {
        //circumvent Folia's thread check by not calling the super.getHandle() method.
        return (FakeEntityHuman) this.entity;
    }

}
