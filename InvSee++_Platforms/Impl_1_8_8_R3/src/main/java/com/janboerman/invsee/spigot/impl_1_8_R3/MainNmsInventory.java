package com.janboerman.invsee.spigot.impl_1_8_R3;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.inventory.AbstractNmsInventory;
import com.janboerman.invsee.utils.Ref;
import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IInventory;
import net.minecraft.server.v1_8_R3.ITileEntityContainer;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

class MainNmsInventory extends AbstractNmsInventory<PlayerInventorySlot, MainBukkitInventory, MainNmsInventory> implements IInventory, ITileEntityContainer {

    protected ItemStack[] storageContents;
    protected ItemStack[] armourContents;
    //Minecraft 1.8.8 has no concept of off-hand!

    protected Ref<ItemStack> onCursor;
    protected ItemStack[] playerCraftingContents;
    private ItemStack[] personalContents;  //crafting, anvil, merchant, enchanting
    private int personalContentsSize;

    MainNmsInventory(EntityHuman target, CreationOptions<PlayerInventorySlot> creationOptions) {
        super(target.getUniqueID(), target.getName(), creationOptions);

        PlayerInventory inv = target.inventory;
        this.storageContents = inv.items;
        this.armourContents = inv.armor;
        //this.offHand = inv.extraSlots; //off-hand did not yet exist in 1.8.8! :O
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

    void setPersonalContents(ItemStack[] personalContents, int size) {
        this.personalContents = personalContents;
        this.personalContentsSize = size;
    }

    void setPersonalContents(ItemStack[] personalContents) {
        setPersonalContents(personalContents, personalContents.length);
    }

    ItemStack[] getPersonalContents() {
        return personalContents;
    }

    int getPersonalContentsSize() {
        return personalContentsSize;
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
        this.onCursor = from.onCursor;
        this.playerCraftingContents = from.playerCraftingContents;
        this.personalContents = from.personalContents;
        this.personalContentsSize = from.personalContentsSize;
        update();
    }

    private Ref<ItemStack> decideWhichItem(int slot) {
        int storageSize = storageContents.length;
        if (0 <= slot && slot < storageSize) {
            int idx = slot;
            return Ref.ofArray(idx, storageContents);
        }

        int armourSize = armourContents.length;
        if (storageSize <= slot && slot < storageSize + armourSize) {
            int idx = slot - storageSize;
            return Ref.ofArray(idx, armourContents);
        }

        int offhandSize = 1; //even though we don't have an offhand slot, we still reserve one (inaccessible) slot for it!

        if (storageSize + armourSize + offhandSize == slot) {
            return onCursor;
        }

        if (45 <= slot && slot < 54) {
            int idx = slot - 45;
            if (idx < personalContentsSize) {
                return Ref.ofArray(idx, personalContents);
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
        if (!InvseeImpl.isEmptyStack(stack) && subtractAmount > 0) {
            ItemStack oldStackCopy = ref.get().cloneAndSubtract(subtractAmount);
            if (InvseeImpl.isEmptyStack(oldStackCopy)) {
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
        if (InvseeImpl.isEmptyStack(stack)) {
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
        if (!InvseeImpl.isEmptyStack(itemStack) && itemStack.count > getMaxStackSize()) {
            itemStack.count = getMaxStackSize();
        }

        update();
    }

    @Override
    public void update() {
        //called after an item in the inventory was removed, added or updated.
        //looking at InventorySubContainer, I don't think we need to do anything here.

        //but we might want to update our viewers? cause of the craftingOrWorkbench switching thing?
        //in that case, we only need to update those specific slots, not the entire inventory.
        for (HumanEntity viewer : getViewers()) {
            if (viewer instanceof Player) {
                ((Player) viewer).updateInventory();
            }
        }
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
    public void b(int idx, int value) { //setProperty
    }

    @Override
    public int g() { //numProperties
        return 0;
    }

    @Override
    public ItemStack[] getContents() {
//        List<ItemStack> paddingOne = NonNullList.a(45 - storageContents.size() - armourContents.size() - offHand.size() - 1, InvseeImpl.EMPTY_STACK);
//        List<ItemStack> paddingTwo = NonNullList.a(9 - personalContents.size(), InvseeImpl.EMPTY_STACK);
//
//        return new ConcatList<>(storageContents,
//                new ConcatList<>(armourContents,
//                        new ConcatList<>(offHand,
//                                new ConcatList<>(new SingletonList<>(onCursor),
//                                        new ConcatList<>(paddingOne,
//                                                new ConcatList<>(personalContents,
//                                                        paddingTwo))))));
        ItemStack[] result = new ItemStack[getSize()];
        System.arraycopy(storageContents, 0, result, PlayerInventorySlot.CONTAINER_00.defaultIndex(), storageContents.length);
        System.arraycopy(armourContents, 0, result, PlayerInventorySlot.ARMOUR_BOOTS.defaultIndex(), armourContents.length);
        result[PlayerInventorySlot.CURSOR.defaultIndex()] = onCursor.get();
        System.arraycopy(personalContents, 0, result, PlayerInventorySlot.PERSONAL_00.defaultIndex(), personalContentsSize);
        return result;
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
    public void l() { //clear
        InvseeImpl.clear(storageContents);
        InvseeImpl.clear(armourContents);
        onCursor.set(InvseeImpl.EMPTY_STACK);
        InvseeImpl.clear(personalContents);
        if (personalContents != playerCraftingContents) {
            InvseeImpl.clear(playerCraftingContents);
        }
    }

    @Override
    public Container createContainer(PlayerInventory playerInventory, EntityHuman entityHuman) {
        EntityPlayer entityPlayer = (EntityPlayer) entityHuman;
        return new MainNmsContainer(entityPlayer.nextContainerCounter(), this, playerInventory, entityHuman, creationOptions);
    }

    @Override
    public String getContainerName() {
        return "minecraft:container";
    }

    @Override
    public String getName() {
        return creationOptions.getTitle().titleFor(Target.byGameProfile(targetPlayerUuid, targetPlayerName));
    }

    @Override
    public boolean hasCustomName() {
        return creationOptions.getTitle() != null;
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return CraftChatMessage.fromString(creationOptions.getTitle().titleFor(Target.byGameProfile(targetPlayerUuid, targetPlayerName)))[0];
    }

}

