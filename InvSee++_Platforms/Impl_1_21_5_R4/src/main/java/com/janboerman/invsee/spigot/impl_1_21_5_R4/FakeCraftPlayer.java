package com.janboerman.invsee.spigot.impl_1_21_5_R4;

import java.util.Optional;

import org.bukkit.craftbukkit.v1_21_R4.CraftServer;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftPlayer;

import net.minecraft.nbt.CompoundTag;

public class FakeCraftPlayer extends CraftPlayer {
    public FakeCraftPlayer(CraftServer server, FakeEntityPlayer entity) {
        super(server, entity);
    }

    /* Normally, CraftPlayer overwrites the bukkit.lastPlayed field with System.currentTimeMillis()
     * For fake players, we want to keep the original value. Same for Paper.LastSeen.
     * This should fix https://github.com/Jannyboy11/InvSee-plus-plus/issues/13. */
    @Override
    public void setExtraData(CompoundTag tag) {
        super.setExtraData(tag);

        Optional<CompoundTag> maybeFreshlyLoaded = loadPlayerTag();
        if (maybeFreshlyLoaded.isPresent()) { //can be absent if the player hasn't played before.
            CompoundTag freshlyLoaded = maybeFreshlyLoaded.get();
            Optional<CompoundTag> maybeBukkit = freshlyLoaded.getCompound("bukkit");
            Optional<CompoundTag> maybePaper = freshlyLoaded.getCompound("Paper");

            CompoundTag loadedBukkit;
            CompoundTag loadedPaper;

            //populate using bukkit's and paper's old values
            if (tag.contains("bukkit") && maybeBukkit.isPresent() && !(loadedBukkit = maybeBukkit.get()).isEmpty())
                tag.getCompound("bukkit").get().putLong("lastPlayed", loadedBukkit.getLong("lastPlayed").get());
            if (tag.contains("Paper") && maybePaper.isPresent() && !(loadedPaper = maybePaper.get()).isEmpty())
                tag.getCompound("Paper").get().putLong("LastSeen", loadedPaper.getLong("LastSeen").get());
        }
    }

    private Optional<CompoundTag> loadPlayerTag() {
        return server.getHandle().playerIo.load(getName(), getUniqueId().toString());
    }

    @Override
    public FakeEntityPlayer getHandle() {
        //circumvent Folia's thread check by not calling the super.getHandle() method.
        return (FakeEntityPlayer) this.entity;
    }

}
