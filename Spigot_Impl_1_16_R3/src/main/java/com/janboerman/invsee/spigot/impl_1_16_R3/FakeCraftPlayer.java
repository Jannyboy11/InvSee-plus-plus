package com.janboerman.invsee.spigot.impl_1_16_R3;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;

public class FakeCraftPlayer extends CraftPlayer {
    public FakeCraftPlayer(CraftServer server, EntityPlayer entity) {
        super(server, entity);
    }

    @Override
    public void setExtraData(NBTTagCompound tag) {
        super.setExtraData(tag);

        NBTTagCompound freshlyLoaded = loadPlayerTag();
        if (freshlyLoaded != null) {
            NBTTagCompound loadedBukkit = freshlyLoaded.getCompound("bukkit");
            NBTTagCompound loadedPaper = freshlyLoaded.getCompound("Paper");

            if (tag.hasKeyOfType("bukkit", 10) && loadedBukkit != null && !loadedBukkit.isEmpty())
                tag.getCompound("bukkit").setLong("lastPlayed", loadedBukkit.getLong("lastPlayed"));
            if (tag.hasKeyOfType("Paper", 10) && loadedPaper != null && !loadedPaper.isEmpty())
                tag.getCompound("Paper").setLong("lastSeen", loadedPaper.getLong("LastSeen"));
        }
    }

    private NBTTagCompound loadPlayerTag() {
        return server.getHandle().playerFileData.getPlayerData(getUniqueId().toString());
    }
}
