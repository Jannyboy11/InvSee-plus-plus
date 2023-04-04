package com.janboerman.invsee.spigot.impl_1_15_R1;

import com.janboerman.invsee.spigot.internal.NBTConstants;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.WorldNBTStorage;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;

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

            if (tag.hasKeyOfType("bukkit", NBTConstants.TAG_COMPOUND) && loadedBukkit != null && !loadedBukkit.isEmpty())
                tag.getCompound("bukkit").setLong("lastPlayed", loadedBukkit.getLong("lastPlayed"));
            if (tag.hasKeyOfType("Paper", NBTConstants.TAG_COMPOUND) && loadedPaper != null && !loadedPaper.isEmpty())
                tag.getCompound("Paper").setLong("LastSeen", loadedPaper.getLong("LastSeen"));
        }
    }

    private NBTTagCompound loadPlayerTag() {
        return ((WorldNBTStorage) server.getHandle().playerFileData).getPlayerData(getUniqueId().toString());
    }
}
