package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.inventory.ShallowCopy;
import com.janboerman.invsee.utils.ConcatList;
import com.janboerman.invsee.utils.ConstantList;
import com.janboerman.invsee.utils.ListHelper;
import com.janboerman.invsee.utils.Ref;
import com.janboerman.invsee.utils.SingletonList;
import net.glowstone.entity.GlowHumanEntity;
import net.glowstone.inventory.GlowCraftingInventory;
import net.glowstone.inventory.GlowInventory;
import net.glowstone.inventory.GlowInventorySlot;
import net.glowstone.inventory.GlowPlayerInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class MainInventory extends GlowInventory implements MainSpectatorInventory, ShallowCopy<MainInventory> {

    final UUID targetPlayerUuid;
    final String targetPlayerName;
    final CreationOptions<PlayerInventorySlot> creationOptions;

    final List<GlowInventorySlot> containerSlots;
    final List<GlowInventorySlot> armourSlots;
    final GlowInventorySlot offhandSlot;
    final GlowInventorySlot cursorSlot;
    final List<GlowInventorySlot> personalSlots;

    public MainInventory(GlowHumanEntity targetPlayer, CreationOptions<PlayerInventorySlot> creationOptions) {
        super(null, InventoryType.CHEST, 54, creationOptions.getTitle().titleFor(Target.byPlayer(targetPlayer)));

        this.targetPlayerUuid = targetPlayer.getUniqueId();
        this.targetPlayerName = targetPlayer.getName();
        this.creationOptions = creationOptions;

        GlowPlayerInventory targetInventory = targetPlayer.getInventory();
        List<GlowInventorySlot> targetSlots = GlowstoneHacks.getSlots(targetInventory);
        this.containerSlots = targetSlots.subList(0, 36);
        this.armourSlots = targetSlots.subList(36, 40);

        GlowInventorySlot offHand;
        try {
            EquipmentSlot.valueOf("OFF_HAND");
            offHand = targetSlots.get(40);
        } catch (IllegalArgumentException e) {
            offHand = InaccessibleSlot.INSTANCE;
        }
        this.offhandSlot = offHand;

        this.cursorSlot = new CursorSlot(targetPlayer);

        //on Glowstone, the GlowCraftingInventory carries the RESULT at slot 0, and the CRAFTING matrix at slots 1 through (size-1).
        GlowCraftingInventory craftingInventory = targetInventory.getCraftingInventory();
        this.personalSlots = GlowstoneHacks.getSlots(craftingInventory).subList(1, craftingInventory.getSize());

        //the ultimate hack! :D
        List<GlowInventorySlot> hack = new ConcatList<>(containerSlots, armourSlots);
        hack = new ConcatList<>(hack, Collections.singletonList(offhandSlot));
        hack = new ConcatList<>(hack, Collections.singletonList(cursorSlot));
        hack = new ConcatList<>(hack, new ConstantList<>(4, InaccessibleSlot.INSTANCE));
        hack = new ConcatList<>(hack, personalSlots);
        if (personalSlots.size() < 9) {
            hack = new ConcatList<>(hack, new ConstantList<>(9 - personalSlots.size(), InaccessibleSlot.INSTANCE));
        }
        GlowstoneHacks.setSlots(this, hack);
    }

    @Override
    public ItemStack[] getArmourContents() {
        return armourSlots.stream().map(GlowInventorySlot::getItem).toArray(ItemStack[]::new);
    }

    @Override
    public void setArmourContents(ItemStack[] armourContents) {
        Objects.requireNonNull(armourContents, "armourContents cannot be null");
        int armourContentsSize = armourSlots.size();
        if (armourContents.length != armourContentsSize)
            throw new IllegalArgumentException("armour contents must be of length " + armourContentsSize);

        for (int i = 0; i < armourContentsSize; i++) {
            armourSlots.get(i).setItem(armourContents[i]);
        }
    }

    @Override
    public ItemStack[] getOffHandContents() {
        if (offhandSlot == InaccessibleSlot.INSTANCE) {
            return new ItemStack[0];
        } else {
            return new ItemStack[] { offhandSlot.getItem() };
        }
    }

    @Override
    public void setOffHandContents(ItemStack[] offHand) {
        if (offHand == null || offHand.length == 0) return;
        offhandSlot.setItem(offHand[0]);
    }

    @Override
    public void setCursorContents(ItemStack cursor) {
        cursorSlot.setItem(cursor);
    }

    @Override
    public ItemStack getCursorContents() {
        return cursorSlot.getItem();
    }

    @Override
    public void setPersonalContents(ItemStack[] craftingContents) {
        Objects.requireNonNull(craftingContents, "craftingContents cannot be null");
        if (craftingContents.length != personalSlots.size())
            throw new IllegalArgumentException("craftingContents must have size " + personalSlots.size());

        for (int i = 0; i < craftingContents.length; i++) {
            personalSlots.get(i).setItem(craftingContents[i]);
        }
    }

    @Override
    public ItemStack[] getPersonalContents() {
        return personalSlots.stream().map(GlowInventorySlot::getItem).toArray(ItemStack[]::new);
    }

    @Override
    public int getPersonalContentsSize() {
        return personalSlots.size();
    }

    //TODO also override getContents (include padding :))
    //TODO also override setContents (hell yes, fun!)

    //

    @Override
    public String getSpectatedPlayerName() {
        return targetPlayerName;
    }

    @Override
    public UUID getSpectatedPlayerId() {
        return targetPlayerUuid;
    }

    @Override
    public CreationOptions<PlayerInventorySlot> getCreationOptions() {
        return creationOptions.clone();
    }

    //

    @Override
    public int defaultMaxStack() {
        return 64;
    }

    @Override
    public void shallowCopyFrom(MainInventory from) {
        GlowstoneHacks.setSlots(this, GlowstoneHacks.getSlots(from));
    }

    //

    @Override
    public void forEach(Consumer<? super ItemStack> action) {
        for (GlowInventorySlot slot : getSlots()) {
            action.accept(slot.getItem());
        }
    }

}
