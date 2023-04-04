package com.janboerman.invsee.spigot.impl_1_17_1_R1;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

public class FakeCraftPlayer extends CraftPlayer {
    public FakeCraftPlayer(CraftServer server, ServerPlayer entity) {
        super(server, entity);
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        super.setExtraData(tag);

        CompoundTag freshlyLoaded = loadPlayerTag();
        if (freshlyLoaded != null) {
            CompoundTag loadedBukkit = freshlyLoaded.getCompound("bukkit");
            CompoundTag loadedPaper = freshlyLoaded.getCompound("Paper");

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
