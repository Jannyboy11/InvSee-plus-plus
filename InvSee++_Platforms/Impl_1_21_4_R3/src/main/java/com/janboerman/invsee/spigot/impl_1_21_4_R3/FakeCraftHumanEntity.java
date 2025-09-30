package com.janboerman.invsee.spigot.impl_1_21_4_R3;

import org.bukkit.craftbukkit.v1_21_R3.CraftServer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftHumanEntity;

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
