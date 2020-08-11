package com.janboerman.invsee.spigot.impl_1_16;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

public class EnderNmsContainer extends Container {

    private final EntityHuman player;
    private final EnderNmsInventory top;
    private final IInventory bottom;

    private InventoryView bukkitView;

    public EnderNmsContainer(int containerId, EnderNmsInventory nmsInventory, PlayerInventory playerInventory, EntityHuman player) {
        super(Containers.GENERIC_9X3, containerId);
        this.player = player;
        this.top = nmsInventory;
        this.bottom = playerInventory;
        //setTitle(nmsInventory.getScoreboardDisplayName());

        //top inventory slots
        for (int yPos = 0; yPos < 3; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 18 + yPos * 18;
                a(new Slot(top, index, magicX, magicY));
            }
        }

        //bottom inventory slots
        int magicAddY = (3 /*3 for 3 rows of the top inventory*/ - 4 /*4 for 4 rows of the bottom inventory??*/) * 18;

        //player 'storage'
        for (int yPos = 1; yPos < 4; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 103 + yPos * 18 + magicAddY;
                a(new Slot(playerInventory, index, magicX, magicY));
            }
        }

        //player 'hotbar'
        for (int xPos = 0; xPos < 9; xPos++) {
            int index = xPos;
            int magicX = 8 + xPos * 18;
            int magicY = 161 + magicAddY;
            a(new Slot(playerInventory, index, magicX, magicY));
        }
    }

    @Override
    public InventoryView getBukkitView() {
        if (bukkitView == null) {
            bukkitView = new CraftInventoryView(player.getBukkitEntity(), top.bukkit, this);
        }
        return bukkitView;
    }

    @Override
    public boolean canUse(EntityHuman entityHuman) {
        return true;
    }
}
