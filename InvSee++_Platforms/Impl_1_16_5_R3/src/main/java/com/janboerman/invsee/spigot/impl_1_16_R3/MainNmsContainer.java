package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.logging.LogOutput;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import net.minecraft.server.v1_16_R3.*;

import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class MainNmsContainer extends Container {

    final EntityHuman player;
    final MainNmsInventory top;
    final PlayerInventory bottom;
    final String title;
    final CreationOptions<PlayerInventorySlot> creationOptions;

    private final boolean spectatingOwnInventory;
    private MainBukkitInventoryView bukkitView;
    final DifferenceTracker tracker;

    private static Slot makeSlot(Mirror<PlayerInventorySlot> mirror, boolean spectatingOwnInventory, MainNmsInventory top, int positionIndex, int magicX, int magicY) {
        final PlayerInventorySlot place = mirror.getSlot(positionIndex);

        if (place == null) {
            return new InaccessibleSlot(top, positionIndex, magicX, magicY);
        } else if (place.isContainer()) {
            final int referringTo = place.ordinal() - PlayerInventorySlot.CONTAINER_00.ordinal();
            return new Slot(top, referringTo, magicX, magicY); //magicX and magicY correct here? it seems to work though.
        } else if (place.isArmour()) {
            final int referringTo = place.ordinal() - PlayerInventorySlot.ARMOUR_BOOTS.ordinal() + 36;
            return new Slot(top, referringTo, magicX, magicY); //idem?
        } else if (place.isPersonal()) {
            final int referringTo = place.ordinal() - PlayerInventorySlot.PERSONAL_00.ordinal() + 45;
            return new PersonalSlot(top, referringTo, magicX, magicY); //idem?
        } else if (place.isOffHand()) {
            final int referringTo = 40;
            return new Slot(top, referringTo, magicX, magicY); //idem?
        } else if (place.isCursor() && !spectatingOwnInventory) {
            final int referringTo = 41;
            return new Slot(top, referringTo, magicX, magicY); //idem?
        } else {
            return new InaccessibleSlot(top, positionIndex, magicX, magicY); //idem?
        }
    }


    //clicked
    @Override
    public ItemStack a(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
        List<org.bukkit.inventory.ItemStack> contentsBefore = null, contentsAfter;
        if (tracker != null) {
            contentsBefore = top.getContents().stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
        }

        ItemStack result = super.a(i, j, inventoryclicktype, entityhuman);

        if (tracker != null) {
            contentsAfter = top.getContents().stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
            tracker.onClick(contentsBefore, contentsAfter);
        }

        return result;
    }

    //removed
    @Override
    public void b(EntityHuman entityhuman) {
        super.b(entityhuman);

        if (tracker != null && Objects.equals(entityhuman, player)) {
            tracker.onClose();
        }
    }

    MainNmsContainer(int containerId, MainNmsInventory nmsInventory, PlayerInventory playerInventory, EntityHuman player, CreationOptions<PlayerInventorySlot> creationOptions) {
        super(Containers.GENERIC_9X6, containerId);
        this.top = nmsInventory;
        this.bottom = playerInventory;
        this.player = player;
        //setTitle(nmsInventory.getScoreboardDisplayName()); //setTitle is actually called when the thing actually opens. or something.
        this.spectatingOwnInventory = player.getUniqueID().equals(nmsInventory.spectatedPlayerUuid);

        //creation options
        this.creationOptions = creationOptions;
        //title
        this.title = creationOptions.getTitle().titleFor(Target.byGameProfile(nmsInventory.spectatedPlayerUuid, nmsInventory.spectatedPlayerName));
        //mirror
        Mirror<PlayerInventorySlot> mirror = creationOptions.getMirror();
        //logging
        LogOptions logOptions = creationOptions.getLogOptions();
        Plugin plugin = creationOptions.getPlugin();
        if (!LogOptions.isEmpty(logOptions)) {
            this.tracker = new DifferenceTracker(
                    LogOutput.make(plugin, player.getUniqueID(), player.getName(), Target.byGameProfile(nmsInventory.spectatedPlayerUuid, nmsInventory.spectatedPlayerName), logOptions),
                    logOptions.getGranularity());
            this.tracker.onOpen();
        } else {
            this.tracker = null;
        }

        // Magma compatability:
        // IT IS IMPORTANT THAT WE CALL super.a AND NOT this.a. THIS IS BECAUSE OF A BUG IN THE MAGMA REMAPPER!
        // https://github.com/Jannyboy11/InvSee-plus-plus/issues/43#issuecomment-1493377971

        //top inventory slots
        for (int yPos = 0; yPos < 6; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 18 + yPos * 18;
                super.a(makeSlot(mirror, spectatingOwnInventory, top, index, magicX, magicY));
            }
        }

        //bottom inventory slots
        int magicAddY = (6 /*6 for 6 rows of the top inventory*/ - 4 /*4 for 4 rows of the bottom inventory??*/) * 18;

        //player 'storage'
        for (int yPos = 1; yPos < 4; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 103 + yPos * 18 + magicAddY;
                super.a(new Slot(playerInventory, index, magicX, magicY));
            }
        }

        //player 'hotbar' (yPos = 0)
        for (int xPos = 0; xPos < 9; xPos++) {
            int index = xPos;
            int magicX = 8 + xPos * 18;
            int magicY = 161 + magicAddY;
            super.a(new Slot(playerInventory, index, magicX, magicY));
        }
    }

    @Override
    public MainBukkitInventoryView getBukkitView() {
        if (bukkitView == null) {
            bukkitView = new MainBukkitInventoryView(this);
        }
        return bukkitView;
    }

    @Override
    public boolean canUse(EntityHuman entityHuman) {
        return true;
    }

    @Override
    public ItemStack shiftClick(EntityHuman entityhuman, int rawIndex) {
        //returns EMPTY_STACK when we are done transferring the itemstack on the rawIndex
        //remember that we are called inside the body of a loop!

        if (spectatingOwnInventory)
            return InvseeImpl.EMPTY_STACK;

        List<Slot> slots = super.slots;

        ItemStack itemstack = InvseeImpl.EMPTY_STACK;
        Slot slot = slots.get(rawIndex);
        final int topRows = 6;

        if (slot != null && slot.hasItem()) {
            ItemStack clickedSlotItem = slot.getItem();

            itemstack = clickedSlotItem.cloneItemStack();
            if (rawIndex < topRows * 9) {
                //clicked in the top inventory
                if (!doShiftClickTransfer(clickedSlotItem, topRows * 9, slots.size(), true)) {
                    return InvseeImpl.EMPTY_STACK;
                }
            } else {
                //clicked in the bottom inventory
                if (!doShiftClickTransfer(clickedSlotItem, 0, topRows * 9, false)) {
                    return InvseeImpl.EMPTY_STACK;
                }
            }

            if (clickedSlotItem.isEmpty()) {
                slot.set(InvseeImpl.EMPTY_STACK);
            } else {
                slot.d();
            }
        }

        return itemstack;
    }

    private boolean doShiftClickTransfer(ItemStack clickedSlotItem, int targetMinIndex, int targetMaxIndex, boolean topClicked) {
        //returns true is something if part of the clickedSlotItem was transferred, otherwise false
        return super.a(clickedSlotItem, targetMinIndex, targetMaxIndex, topClicked);
    }


    // ===== Magma Compatibility =====
    // https://github.com/Jannyboy11/InvSee-plus-plus/issues/43#issuecomment-1493377971

    //because this is being called from InvSee++, and Magma has a bug, we need to override this.
    public Containers<?> getType() {
        return Containers.GENERIC_9X6;
    }

    //idem.
    public void addSlotListener(ICrafting icrafting) {
        super.addSlotListener(icrafting);
    }

    public boolean func_75145_c(EntityHuman playerEntity) {
        return canUse(playerEntity);
    }

    public ItemStack func_184996_a(int a, int b, InventoryClickType clickType, EntityHuman playerEntity) {
        //clicked
        return a(a, b, clickType, playerEntity);
    }

    public void func_75134_a(EntityHuman playerEntity) {
        //removed
        b(playerEntity);
    }

    public ItemStack func_82846_b(EntityHuman playerEntity, int rawIndex) {
        return shiftClick(playerEntity, rawIndex);
    }

}
