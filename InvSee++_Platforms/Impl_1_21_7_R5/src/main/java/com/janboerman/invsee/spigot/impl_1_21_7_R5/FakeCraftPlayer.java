package com.janboerman.invsee.spigot.impl_1_21_7_R5;

import java.util.Optional;

import org.bukkit.craftbukkit.v1_21_R5.CraftServer;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FakeCraftPlayer extends CraftPlayer {
    public FakeCraftPlayer(CraftServer server, FakeEntityPlayer entity) {
        super(server, entity);
    }

    /* Normally, CraftPlayer overwrites the bukkit.lastPlayed field with System.currentTimeMillis()
     * For fake players, we want to keep the original value. Same for Paper.LastSeen.
     * This should fix https://github.com/Jannyboy11/InvSee-plus-plus/issues/13. */
    @Override
    public void setExtraData(ValueOutput tag) {
        super.setExtraData(tag);

        Optional<ValueInput> maybeFreshlyLoaded = loadPlayerTag();
        if (maybeFreshlyLoaded.isPresent()) { //can be absent if the player hasn't played before.
            ValueInput freshlyLoaded = maybeFreshlyLoaded.get();
            Optional<ValueInput> readBukkit = freshlyLoaded.child("bukkit");
            Optional<ValueInput> readPaper = freshlyLoaded.child("Paper");

            ValueOutput writeBukkit = tag.child("bukkit");
            ValueOutput writePaper = tag.child("Paper");

            //populate using bukkit's and paper's old values
            copyLong(readBukkit, writeBukkit, "lastPlayed");
            copyLong(readPaper, writePaper, "LastSeen");
        }
    }

    private static void copyLong(Optional<ValueInput> from, ValueOutput to, String key) {
        from.flatMap(input -> input.getLong(key)).ifPresent(value -> to.putLong(key, value));
    }

    private Optional<ValueInput> loadPlayerTag() {
        return HybridServerSupport.load(server.getHandle().playerIo, getName(), getUniqueId().toString(), ThrowingProblemReporter.INSTANCE, getHandle().registryAccess());
    }

    @Override
    public FakeEntityPlayer getHandle() {
        //circumvent Folia's thread check by not calling the super.getHandle() method.
        return (FakeEntityPlayer) this.entity;
    }

}
