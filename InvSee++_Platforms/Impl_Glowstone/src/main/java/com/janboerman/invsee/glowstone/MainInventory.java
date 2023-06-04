package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.inventory.Personal;
import com.janboerman.invsee.spigot.internal.inventory.ShallowCopy;
import com.janboerman.invsee.utils.ConcatList;
import com.janboerman.invsee.utils.ConstantList;
import net.glowstone.entity.GlowHumanEntity;
import net.glowstone.inventory.GlowAnvilInventory;
import net.glowstone.inventory.GlowCraftingInventory;
import net.glowstone.inventory.GlowEnchantingInventory;
import net.glowstone.inventory.GlowInventory;
import net.glowstone.inventory.GlowInventorySlot;
import net.glowstone.inventory.GlowMerchantInventory;
import net.glowstone.inventory.GlowPlayerInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

class MainInventory extends GlowInventory implements MainSpectatorInventory, ShallowCopy<MainInventory>, Personal {

    final UUID targetPlayerUuid;
    final String targetPlayerName;
    final CreationOptions<PlayerInventorySlot> creationOptions;

    final List<GlowInventorySlot> containerSlots;
    final List<GlowInventorySlot> armourSlots;
    final GlowInventorySlot offhandSlot;
    GlowInventorySlot cursorSlot;
    final List<GlowInventorySlot> craftingSlots;
    List<GlowInventorySlot> personalSlots;

    MainInventory(GlowHumanEntity targetPlayer, CreationOptions<PlayerInventorySlot> creationOptions) {
        super(null, InventoryType.CHEST, 54, creationOptions.getTitle().titleFor(Target.byPlayer(targetPlayer)));

        this.targetPlayerUuid = targetPlayer.getUniqueId();
        this.targetPlayerName = targetPlayer.getName();
        this.creationOptions = creationOptions;
        setMaxStackSize(defaultMaxStack());

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
        this.personalSlots = this.craftingSlots = GlowstoneHacks.getSlots(craftingInventory).subList(1, craftingInventory.getSize());

        //the ultimate hack! :D
        updateContents();
    }

    void updateContents() {
        List<GlowInventorySlot> list = new ConcatList<>(containerSlots, armourSlots);
        list = new ConcatList<>(list, Collections.singletonList(offhandSlot));
        list = new ConcatList<>(list, Collections.singletonList(cursorSlot));
        list = new ConcatList<>(list, new ConstantList<>(3, InaccessibleSlot.INSTANCE));
        list = new ConcatList<>(list, personalSlots);
        if (personalSlots.size() < 9) {
            list = new ConcatList<>(list, new ConstantList<>(9 - personalSlots.size(), InaccessibleSlot.INSTANCE));
        }
        GlowstoneHacks.setSlots(this, list);
    }

    @Override
    public ItemStack[] getStorageContents() {
        return containerSlots.stream().map(GlowInventorySlot::getItem).toArray(ItemStack[]::new);
    }

    @Override
    public void setStorageContents(ItemStack[] storageContents) {
        Objects.requireNonNull(storageContents, "storageContents cannot be null");
        int storageContentsSize = containerSlots.size();
        if (storageContentsSize != storageContents.length)
            throw new IllegalArgumentException("storage contents must be of length " + storageContentsSize);

        for (int i = 0; i < storageContentsSize; i++) {
            containerSlots.get(i).setItem(storageContents[i]);
        }

        updateContents();
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

        updateContents();
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

        updateContents();
    }

    @Override
    public void setCursorContents(ItemStack cursor) {
        cursorSlot.setItem(cursor);

        updateContents();
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

        updateContents();
    }

    @Override
    public ItemStack[] getPersonalContents() {
        return personalSlots.stream().map(GlowInventorySlot::getItem).toArray(ItemStack[]::new);
    }

    @Override
    public int getPersonalContentsSize() {
        return personalSlots.size();
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] result = new ItemStack[getSize()];
        System.arraycopy(getStorageContents(), 0, result, 0, 36);
        System.arraycopy(getArmourContents(), 0, result, 36, 4);
        result[40] = offhandSlot.getItem();
        result[41] = cursorSlot.getItem();
        System.arraycopy(getPersonalContents(), 0, result, 45, getPersonalContentsSize());
        return result;
    }

    @Override
    public void setContents(ItemStack[] items) {
        for (int i = 0; i < 36; i++) {
            containerSlots.get(i).setItem(items[i]);
        }
        for (int i = 0; i < 4; i++){
            armourSlots.get(i).setItem(items[36 + i]);
        }
        offhandSlot.setItem(items[40]);
        cursorSlot.setItem(items[41]);
        for (int i = 0; i < 9; i++) {
            personalSlots.get(i).setItem(items[45 + 1]);
        }

        updateContents();
    }

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

    //

    @Override
    public void watch(InventoryView targetPlayerView) {
        //if the target player has an opened inventory, and if the opened inventory is personal,
        //then set the personalSlots to the be the *same* slots as that opened inventory.
        Inventory top = targetPlayerView.getTopInventory();
        if (top instanceof GlowCraftingInventory) {
            List<GlowInventorySlot> craftingInventorySlots = GlowstoneHacks.getSlots((GlowCraftingInventory) top);
            this.personalSlots = craftingInventorySlots.subList(1, craftingInventorySlots.size());
        } else if (top instanceof GlowAnvilInventory) {
            List<GlowInventorySlot> anvilInventorySlots = GlowstoneHacks.getSlots((GlowAnvilInventory) top);
            this.personalSlots = anvilInventorySlots.subList(0, anvilInventorySlots.size() - 1);
        } else if (top instanceof GlowEnchantingInventory) {
            List<GlowInventorySlot> enchantingInventorySlots = GlowstoneHacks.getSlots((GlowEnchantingInventory) top);
            this.personalSlots = enchantingInventorySlots;
        } else if (top instanceof GlowMerchantInventory) {
            List<GlowInventorySlot> merchantInventorySlots = GlowstoneHacks.getSlots((GlowMerchantInventory) top);
            this.personalSlots = merchantInventorySlots.subList(0, merchantInventorySlots.size() - 1);
        }
        updateContents();

        for (HumanEntity viewer : getViewers()) {
            if (viewer instanceof Player) {
                ((Player) viewer).updateInventory();
            }
        }
    }

    @Override
    public void unwatch() {
        //reset the personal slots back to the crafting contents
        this.personalSlots = craftingSlots;
        updateContents();

        for (HumanEntity viewer : getViewers()) {
            if (viewer instanceof org.bukkit.entity.Player) {
                ((org.bukkit.entity.Player) viewer).updateInventory();
            }
        }
    }

    // Glowstone faulty implementations overrides

    @Override
    public boolean containsAtLeast(ItemStack stack, int amount) {
        if (amount <= 0)
            return true;
        if (stack == null)
            return false; //this is a bit weird, but this is what CraftInventory does.

        int encountered = 0;
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item != null && item.isSimilar(stack)) {
                encountered += item.getAmount();
                if (encountered >= amount)
                    return true;
            }
        }

        return false;
    }


}
