package com.janboerman.invsee.spigot.impl_1_18_2_R2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;

public class FakeCraftPlayer extends CraftPlayer {
    public FakeCraftPlayer(CraftServer server, ServerPlayer entity) {
        super(server, entity);
    }

    /* Normally, CraftPlayer overwrites the bukkit.lastPlayed field with System.currentTimeMillis()
     * For fake players, we want to keep the original value. Same for Paper.LastSeen.
     * This should fix https://github.com/Jannyboy11/InvSee-plus-plus/issues/13. */
    @Override
    public void setExtraData(CompoundTag tag) {
        super.setExtraData(tag);

        CompoundTag freshlyLoaded = loadPlayerTag();
        if (freshlyLoaded != null) { //can be null if the player hasn't played before.
            CompoundTag loadedBukkit = freshlyLoaded.getCompound("bukkit");
            CompoundTag loadedPaper = freshlyLoaded.getCompound("Paper");

            //populate using bukkit's and paper's old values
            if (tag.contains("bukkit", Tag.TAG_COMPOUND) && loadedBukkit != null && !loadedBukkit.isEmpty())
                tag.getCompound("bukkit").putLong("lastPlayed", loadedBukkit.getLong("lastPlayed"));
            if (tag.contains("Paper", Tag.TAG_COMPOUND) && loadedPaper != null && !loadedPaper.isEmpty())
                tag.getCompound("Paper").putLong("LastSeen", loadedPaper.getLong("LastSeen"));
        }
    }

    private CompoundTag loadPlayerTag() {
        return server.getHandle().playerIo.getPlayerData(getUniqueId().toString());
    }

}
