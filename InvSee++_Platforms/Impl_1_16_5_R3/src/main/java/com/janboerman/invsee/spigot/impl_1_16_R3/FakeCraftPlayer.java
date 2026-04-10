package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.internal.NBTConstants;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;

public class FakeCraftPlayer extends CraftPlayer {
    public FakeCraftPlayer(CraftServer server, EntityPlayer entity) {
        super(server, entity);
    }

    /* Normally, CraftPlayer overwrites the bukkit.lastPlayed field with System.currentTimeMillis()
     * For fake players, we want to keep the original value. Same for Paper.LastSeen.
     * This should fix https://github.com/Jannyboy11/InvSee-plus-plus/issues/13. */
    @Override
    public void setExtraData(NBTTagCompound tag) {
        super.setExtraData(tag);

        NBTTagCompound freshlyLoaded = loadPlayerTag();
        if (freshlyLoaded != null) {
            NBTTagCompound loadedBukkit = freshlyLoaded.getCompound("bukkit");
            NBTTagCompound loadedPaper = freshlyLoaded.getCompound("Paper");
            // https://github.com/Jannyboy11/InvSee-plus-plus/issues/193
            NBTTagCompound loadedRootVehicle = freshlyLoaded.getCompound("RootVehicle");

            if (tag.hasKeyOfType("bukkit", NBTConstants.TAG_COMPOUND) && loadedBukkit != null && !loadedBukkit.isEmpty())
                tag.getCompound("bukkit").setLong("lastPlayed", loadedBukkit.getLong("lastPlayed"));
            if (tag.hasKeyOfType("Paper", NBTConstants.TAG_COMPOUND) && loadedPaper != null && !loadedPaper.isEmpty())
                tag.getCompound("Paper").setLong("LastSeen", loadedPaper.getLong("LastSeen"));
            if (loadedRootVehicle != null && !loadedRootVehicle.isEmpty())
                tag.set("RootVehicle", loadedRootVehicle);
        }
    }

    private NBTTagCompound loadPlayerTag() {
        return server.getHandle().playerFileData.getPlayerData(getUniqueId().toString());
    }
}
