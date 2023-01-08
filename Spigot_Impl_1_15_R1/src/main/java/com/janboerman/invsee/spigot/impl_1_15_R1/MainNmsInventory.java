package com.janboerman.invsee.spigot.impl_1_15_R1;

import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.inventory.ShallowCopy;
import com.janboerman.invsee.utils.ConcatList;
import com.janboerman.invsee.utils.Ref;
import com.janboerman.invsee.utils.SingletonList;
import com.janboerman.invsee.utils.UUIDHelper;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.janboerman.invsee.spigot.impl_1_15_R1.InvseeImpl.EMPTY_STACK;

public class MainNmsInventory extends TileEntityContainer /* cannot extend AbstractNmsInventory unfortunately */ implements ShallowCopy<MainNmsInventory> {

    private static final TileEntityTypes TileEntityTypeFakePlayerInventory = new TileEntityTypes(MainNmsInventory::new, Set.of(), new com.mojang.datafixers.types.constant.NilSave());

    protected final UUID spectatedPlayerUuid;
    protected final String spectatedPlayerName;
    protected NonNullList<ItemStack> storageContents;
    protected NonNullList<ItemStack> armourContents;
    protected NonNullList<ItemStack> offHand;

    protected Ref<ItemStack> onCursor;
    protected List<ItemStack> playerCraftingContents;
    protected List<ItemStack> personalContents;  //crafting, anvil, smithing, grindstone, stone cutter, loom, merchant, enchanting

    private MainBukkitInventory bukkit;
    protected String title;
    protected Mirror<PlayerInventorySlot> mirror;

    private int maxStack = IInventory.MAX_STACK;
    private final List<HumanEntity> transaction = new ArrayList<>();
    protected InventoryHolder owner;

    private MainNmsInventory() { //used for the fake tile entity type
        super(TileEntityTypeFakePlayerInventory);
        spectatedPlayerUuid = null;
        spectatedPlayerName = null;
    }

    MainNmsInventory(EntityHuman target, String title, Mirror<PlayerInventorySlot> mirror) {
        // Possibly could've used TileEntityTypes.CHEST, but I'm afraid that will cause troubles elsewhere.
        // So use the fake type for now.
        // All of this hadn't been necessary if craftbukkit checked whether the inventory was an instance of ITileEntityContainer instead of straight up TileEntityContainer.
        super(TileEntityTypeFakePlayerInventory);
        this.spectatedPlayerUuid = UUIDHelper.copy(target.getUniqueID());
        this.spectatedPlayerName = target.getName();
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

        this.title = title;
        this.mirror = mirror;
    }

    public MainBukkitInventory bukkit() {
        return bukkit == null ? bukkit = new MainBukkitInventory(this) : bukkit;
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
    public boolean isEmpty() {
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
        if (ref == null) return EMPTY_STACK;

        return ref.get();
    }

    @Override
    public ItemStack splitStack(int slot, int subtractAmount) {
        Ref<ItemStack> ref = decideWhichItem(slot);
        if (ref == null) return EMPTY_STACK;

        ItemStack stack = ref.get();
        if (!stack.isEmpty() && subtractAmount > 0) {
            ItemStack oldStackCopy = ref.get().cloneAndSubtract(subtractAmount);
            if (!oldStackCopy.isEmpty()) {
                update();
            }
            return oldStackCopy;
        } else {
            return EMPTY_STACK;
        }
    }

    @Override
    public ItemStack splitWithoutUpdate(int slot) {
        Ref<ItemStack> ref = decideWhichItem(slot);
        if (ref == null) return EMPTY_STACK;

        ItemStack stack = ref.get();
        if (stack.isEmpty()) {
            return EMPTY_STACK;
        } else {
            ref.set(EMPTY_STACK);
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
    public int getMaxStackSize() {
        return maxStack;
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
    public List<ItemStack> getContents() {
        List<ItemStack> paddingOne = NonNullList.a(45 - storageContents.size() - armourContents.size() - offHand.size() - 1, EMPTY_STACK);
        List<ItemStack> paddingTwo = NonNullList.a(9 - personalContents.size(), EMPTY_STACK);

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
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return owner;
    }

    @Override
    public void setMaxStackSize(int maxStack) {
        this.maxStack = maxStack;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void clear() {
        storageContents.clear();
        armourContents.clear();
        offHand.clear();
        onCursor.set(EMPTY_STACK);
        playerCraftingContents.clear();
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return CraftChatMessage.fromStringOrNull(title);
    }

    @Override
    protected IChatBaseComponent getContainerName() {
        return new ChatMessage("minecraft:generic_9x6");
    }

    @Override
    protected Container createContainer(int containerId, PlayerInventory playerInventory) {
        return new MainNmsContainer(containerId, this, playerInventory, playerInventory.player, mirror);
    }

    @Override
    public boolean e(EntityHuman entityhuman) {
        //there is no chestlock here
        return true;
    }

}
