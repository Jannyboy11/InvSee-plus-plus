package com.janboerman.invsee.spigot.impl_1_19_4_R3;

import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;

public class FakeCraftHumanEntity extends CraftHumanEntity {

    public FakeCraftHumanEntity(CraftServer server, FakeEntityHuman entity) {
        super(server, entity);
    }

    @Override
    public FakeEntityHuman getHandle() {
        return (FakeEntityHuman) this.entity;
    }

}
