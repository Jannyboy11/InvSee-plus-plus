package com.janboerman.invsee.spigot.impl_1_12_R1;

import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.inventory.AbstractNmsInventory;
import com.janboerman.invsee.utils.ConcatList;
import com.janboerman.invsee.utils.Ref;
import com.janboerman.invsee.utils.SingletonList;
import net.minecraft.server.v1_12_R1.Container;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.IInventory;
import net.minecraft.server.v1_12_R1.ITileEntityContainer;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NonNullList;
import net.minecraft.server.v1_12_R1.PlayerInventory;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.inventory.Inventory;

import java.util.List;

class MainNmsInventory extends AbstractNmsInventory<PlayerInventorySlot, MainBukkitInventory, MainNmsInventory> implements IInventory, ITileEntityContainer {

    protected NonNullList<ItemStack> storageContents;
    protected NonNullList<ItemStack> armourContents;
    protected NonNullList<ItemStack> offHand;

    protected Ref<ItemStack> onCursor;
    protected List<ItemStack> playerCraftingContents;
    protected List<ItemStack> personalContents;  //crafting, anvil, merchant, enchanting

    MainNmsInventory(EntityHuman target, String title, Mirror<PlayerInventorySlot> mirror) {
        super(target.getUniqueID(), target.getName(), title, mirror);

        PlayerInventory inv = target.inventory;
        this.storageContents = inv.items;
        this.armourContents = inv.armor;
        this.offHand = inv.extraSlots;
        this.onCursor = new Ref<>() {
            @Override
            public void set(ItemStack item) {
                inv.setCarried(item);
            }

            @Override
            public ItemStack get() {
                return inv.getCarried();
            }
        };
        IInventory /*InventoryCrafting*/ playerCrafting = ((CraftInventory) target.defaultContainer.getBukkitView().getTopInventory()).getInventory();
        this.personalContents = this.playerCraftingContents = playerCrafting.getContents();
    }

    @Override
    protected MainBukkitInventory createBukkit() {
        return new MainBukkitInventory(this);
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public int defaultMaxStack() {
        return IInventory.MAX_STACK;
    }

    @Override
    public void shallowCopyFrom(MainNmsInventory from) {
        setMaxStackSize(from.getMaxStackSize());
        this.storageContents = from.storageContents;
        this.armourContents = from.armourContents;
        this.offHand = from.offHand;
        this.onCursor = from.onCursor;
        this.playerCraftingContents = from.playerCraftingContents;
        this.personalContents = from.personalContents;
        update();
    }

    private Ref<ItemStack> decideWhichItem(int slot) {
        int storageSize = storageContents.size();
        if (0 <= slot && slot < storageSize) {
            int idx = slot;
            return Ref.ofList(idx, storageContents);
        }

        int armourSize = armourContents.size();
        if (storageSize <= slot && slot < storageSize + armourSize) {
            int idx = slot - storageSize;
            return Ref.ofList(idx, armourContents);
        }

        int offhandSize = offHand.size();
        if (storageSize + armourSize <= slot && slot < storageSize + armourSize + offhandSize) {
            int idx = slot - storageSize - armourSize;
            return Ref.ofList(idx, offHand);
        }

        if (storageSize + armourSize + offhandSize == slot) {
            return onCursor;
        }

        if (45 <= slot && slot < 54) {
            int idx = slot - 45;
            if (idx < personalContents.size()) {
                return Ref.ofList(idx, personalContents);
            }
        }

        return null;
    }

    @Override
    public int getSize() {
        //includes the non-interactable slots
        return 54;
    }

    @Override
    public boolean x_() { // isEmpty
        for (ItemStack stack : armourContents) {
            if (!stack.isEmpty()) return false;
        }
        for (ItemStack stack : storageContents) {
            if (!stack.isEmpty()) return false;
        }
        for (ItemStack stack : offHand) {
            if (!stack.isEmpty()) return false;
        }
        for (ItemStack stack : personalContents) {
            if (!stack.isEmpty()) return false;
        }
        if (!onCursor.get().isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        Ref<ItemStack> ref = decideWhichItem(slot);
        if (ref == null) return InvseeImpl.EMPTY_STACK;

        return ref.get();
    }

    @Override
    public ItemStack splitStack(int slot, int subtractAmount) {
        Ref<ItemStack> ref = decideWhichItem(slot);
        if (ref == null) return InvseeImpl.EMPTY_STACK;

        ItemStack stack = ref.get();
        if (!stack.isEmpty() && subtractAmount > 0) {
            ItemStack oldStackCopy = ref.get().cloneAndSubtract(subtractAmount);
            if (!oldStackCopy.isEmpty()) {
                update();
            }
            return oldStackCopy;
        } else {
            return InvseeImpl.EMPTY_STACK;
        }
    }

    @Override
    public ItemStack splitWithoutUpdate(int slot) {
        Ref<ItemStack> ref = decideWhichItem(slot);
        if (ref == null) return InvseeImpl.EMPTY_STACK;

        ItemStack stack = ref.get();
        if (stack.isEmpty()) {
            return InvseeImpl.EMPTY_STACK;
        } else {
            ref.set(InvseeImpl.EMPTY_STACK);
            return stack;
        }
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        Ref<ItemStack> ref = decideWhichItem(slot);
        if (ref == null) return;

        ref.set(itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }

        update();
    }

    @Override
    public void update() {
        //called after an item in the inventory was removed, added or updated.
        //looking at InventorySubContainer, I don't think we need to do anything here.

        //but we might want to update our viewers? cause of the craftingOrWorkbench switching thing?
        //in that case, we only need to update those specific slots, not the entire inventory.
//        for (HumanEntity viewer : getViewers()) {
//            if (viewer instanceof Player) {
//                ((Player) viewer).updateInventory();
//            }
//        }
    }

    @Override
    public boolean a(EntityHuman entityHuman) {
        return true;
    }

    @Override
    public void startOpen(EntityHuman entityHuman) {
        onOpen(entityHuman.getBukkitEntity());
    }

    @Override
    public void closeContainer(EntityHuman entityHuman) {
        onClose(entityHuman.getBukkitEntity());
    }

    @Override
    public boolean b(int slot, ItemStack itemStack) { //allowHopperInput
        return true;
    }

    @Override
    public int getProperty(int idx) {
        return 0;
    }

    @Override
    public void setProperty(int idx, int value) {

    }

    @Override
    public int h() { //numProperties
        return 0;
    }

    @Override
    public List<ItemStack> getContents() {
        List<ItemStack> paddingOne = NonNullList.a(45 - storageContents.size() - armourContents.size() - offHand.size() - 1, InvseeImpl.EMPTY_STACK);
        List<ItemStack> paddingTwo = NonNullList.a(9 - personalContents.size(), InvseeImpl.EMPTY_STACK);

        return new ConcatList<>(storageContents,
                new ConcatList<>(armourContents,
                        new ConcatList<>(offHand,
                                new ConcatList<>(new SingletonList<>(onCursor),
                                        new ConcatList<>(paddingOne,
                                                new ConcatList<>(personalContents,
                                                        paddingTwo))))));
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        super.onOpen(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        super.onClose(who);
    }

    @Override
    public void clear() {
        storageContents.clear();
        armourContents.clear();
        offHand.clear();
        onCursor.set(InvseeImpl.EMPTY_STACK);
        playerCraftingContents.clear();
    }

    @Override
    public Container createContainer(PlayerInventory playerInventory, EntityHuman entityHuman) {
        EntityPlayer entityPlayer = (EntityPlayer) entityHuman;
        return new MainNmsContainer(entityPlayer.nextContainerCounter(), this, playerInventory, entityHuman, mirror);
    }

    @Override
    public String getContainerName() {
        return "minecraft:container";
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public boolean hasCustomName() {
        return title != null;
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return CraftChatMessage.fromString(title)[0];
    }
}

