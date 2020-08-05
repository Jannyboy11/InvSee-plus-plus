package com.janboerman.invsee.spigot.impl_1_16;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;

public class BukkitInventory extends CraftInventory implements SpectatorInventory {

    protected BukkitInventory(NmsInventory nmsInventory) {
        super(nmsInventory);
    }

    @Override
    public NmsInventory getInventory() {
        return (NmsInventory) super.getInventory();
    }

    @Override
    public UUID getSpectatedPlayer() {
        return getInventory().spectatedPlayerUuid;
    }

    @Override
    public ItemStack[] getStorageContents() {
        return getInventory().storageContents.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
    }

    @Override
    public void setStorageContents(ItemStack[] storageContents) {
        Objects.requireNonNull(storageContents, "storageContents cannot be null");
        int storageContentsSize = getInventory().storageContents.size();
        if (storageContents.length != storageContentsSize)
            throw new IllegalArgumentException("storageContents must be of length " + storageContentsSize);

        for (int i = 0; i < storageContentsSize; i++) {
            getInventory().storageContents.set(i, CraftItemStack.asNMSCopy(storageContents[i]));
        }
    }

    @Override
    public ItemStack[] getArmourContents() {
        return getInventory().armourContents.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
    }

    @Override
    public void setArmourContents(ItemStack[] armourContents) {
        Objects.requireNonNull(armourContents, "armourContents cannot be null");
        int armourContententsSize = getInventory().armourContents.size();
        if (armourContents.length != armourContententsSize)
            throw new IllegalArgumentException("armourContents must be of length " + armourContententsSize);

        for (int i = 0; i < armourContententsSize; i++) {
            getInventory().armourContents.set(i, CraftItemStack.asNMSCopy(armourContents[i]));
        }
    }

    @Override
    public ItemStack[] getOffHandContents() {
        return getInventory().offHand.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
    }

    @Override
    public void setOffHandContents(ItemStack[] offHand){
        Objects.requireNonNull(offHand, "offHand cannot be null");
        int offHandContentsSize = getInventory().offHand.size();
        if (offHand.length != offHandContentsSize)
            throw new IllegalArgumentException("offHand must be of length " + offHandContentsSize);

        for (int i = 0; i < offHandContentsSize; i++) {
            getInventory().offHand.set(i, CraftItemStack.asNMSCopy(offHand[i]));
        }
    }

}
