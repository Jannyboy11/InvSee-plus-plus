package com.janboerman.invsee.spigot.impl_1_19_3_R2;

import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.inventory.AbstractNmsInventory;
import com.janboerman.invsee.utils.ConcatList;
import com.janboerman.invsee.utils.Ref;
import com.janboerman.invsee.utils.SingletonList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftChatMessage;

import java.util.List;

class MainNmsInventory extends AbstractNmsInventory<PlayerInventorySlot, MainNmsInventory> implements Container, MenuProvider {

	protected NonNullList<ItemStack> storageContents;
	protected NonNullList<ItemStack> armourContents;
	protected NonNullList<ItemStack> offHand;
	
	protected Ref<ItemStack> onCursor;
	protected List<ItemStack> craftingContents;
	protected List<ItemStack> personalContents;  //crafting, anvil, smithing, grindstone, stone cutter, loom, merchant, enchanting
	
	protected MainNmsInventory(Player target, String title, Mirror<PlayerInventorySlot> mirror) {
		super(target.getUUID(), target.getScoreboardName(), title, mirror);
		Inventory inv = target.getInventory();
		this.storageContents = inv.items;
		this.armourContents = inv.armor;
		this.offHand = inv.offhand;
		this.onCursor = new Ref<>() {
			@Override public void set(ItemStack item) { target.containerMenu.setCarried(item); }
			@Override public ItemStack get() { return target.containerMenu.getCarried(); }
		};
		this.personalContents = this.craftingContents = target.inventoryMenu.getCraftSlots().getContents(); //luckily getContents() does not copy
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
		return Container.LARGE_MAX_STACK_SIZE;
	}

	@Override
	public void shallowCopyFrom(MainNmsInventory from) {
		setMaxStackSize(from.getMaxStackSize());
		this.storageContents = from.storageContents;
		this.armourContents = from.armourContents;
		this.offHand = from.offHand;
		this.onCursor = from.onCursor;
		this.craftingContents = from.craftingContents;
		this.personalContents = from.personalContents;
		setChanged();
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
		
		int offHandSize = offHand.size();
		if (storageSize + armourSize <= slot && slot < storageSize + armourSize + offHandSize) {
			int idx = slot - storageSize - armourSize;
			return Ref.ofList(idx, offHand);
		}
		
		if (storageSize + armourSize + offHandSize == slot) {
			return onCursor;
		}
		
		if (45 <= slot && slot < 54) {
			int idx = slot - 45;
			if (idx < personalContents.size()) {
				return Ref.ofList(idx,  personalContents);
			}
		}
		
		return null;
	}

	@Override
	public void clearContent() {
		storageContents.clear();
		armourContents.clear();
		offHand.clear();
		onCursor.set(ItemStack.EMPTY);
		personalContents.clear();
		if (craftingContents != personalContents) {
			craftingContents.clear();
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player viewer) {
		return new MainNmsContainer(containerId, this, playerInventory, viewer, mirror);
	}

	@Override
	public Component getDisplayName() {
		//return new TextComponent("minecraft:generic_9x6");
		return CraftChatMessage.fromStringOrNull(title);
	}

	@Override
	public int getContainerSize() {
		return 54;
	}

	@Override
	public List<ItemStack> getContents() {
		List<ItemStack> paddingOne = NonNullList.withSize(45 - storageContents.size() - armourContents.size() - offHand.size() - 1, ItemStack.EMPTY);
		List<ItemStack> paddingTwo = NonNullList.withSize(9 - personalContents.size(), ItemStack.EMPTY);
		
		return new ConcatList<>(storageContents,
				new ConcatList<>(armourContents,
						new ConcatList<>(offHand,
								new ConcatList<>(new SingletonList<>(onCursor),
										new ConcatList<>(paddingOne,
											new ConcatList<>(personalContents,
													paddingTwo))))));
	}

	@Override
	public ItemStack getItem(int slot) {
		var ref = decideWhichItem(slot);
		if (ref == null) return ItemStack.EMPTY;
		
		return ref.get();
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
		if (!onCursor.get().isEmpty()) return false;
		
		return true;
	}

	@Override
	public void onClose(CraftHumanEntity bukkitPlayer) {
		super.onClose(bukkitPlayer);
	}

	@Override
	public void onOpen(CraftHumanEntity bukkitPlayer) {
		super.onOpen(bukkitPlayer);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		var ref = decideWhichItem(slot);
		if (ref == null) return ItemStack.EMPTY;
		
		ItemStack stack = ref.get();
		if (!stack.isEmpty() && amount > 0) {
			ItemStack oldStackCopy = ref.get().split(amount);
			if (!oldStackCopy.isEmpty()) {
				setChanged();
			}
			return oldStackCopy;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		var ref = decideWhichItem(slot);
		if (ref == null) return ItemStack.EMPTY;
		
		ItemStack stack = ref.get();
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			ref.set(ItemStack.EMPTY);
			return stack;
		}
	}

	@Override
	public void setChanged() {
		//nothing to do here?
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		var ref = decideWhichItem(slot);
		if (ref == null) return;
		
		ref.set(stack);
		if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
			stack.setCount(getMaxStackSize());
		}
		
		setChanged();
	}

	@Override
	public boolean stillValid(Player arg0) {
		return true;
	}

}
