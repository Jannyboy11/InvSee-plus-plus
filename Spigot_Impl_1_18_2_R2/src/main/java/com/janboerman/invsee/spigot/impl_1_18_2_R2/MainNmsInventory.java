package com.janboerman.invsee.spigot.impl_1_18_2_R2;

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
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class MainNmsInventory implements Container, MenuProvider {
	
	protected final UUID targetPlayerUuid;
	protected final String targetPlayerName;
	protected final NonNullList<ItemStack> storageContents;
	protected final NonNullList<ItemStack> armourContents;
	protected final NonNullList<ItemStack> offHand;
	
	protected final Ref<ItemStack> onCursor;
	protected final List<ItemStack> craftingContents;
	protected List<ItemStack> personalContents;  //crafting, anvil, smithing, grindstone, stone cutter, loom, merchant, enchanting
	
	protected org.bukkit.inventory.Inventory bukkit;
	protected String title;
	
	private int maxStack = Container.MAX_STACK;
	private final List<HumanEntity> transaction = new ArrayList<>();
	protected InventoryHolder owner;
	
	protected MainNmsInventory(Player target) {
		this.targetPlayerUuid = target.getUUID();
		this.targetPlayerName = target.getScoreboardName();
		Inventory inv = target.getInventory();
		this.storageContents = inv.items;
		this.armourContents = inv.armor;
		this.offHand = inv.offhand;
		this.onCursor = new Ref<>() {
			@Override
			public void set(ItemStack item) {
				target.containerMenu.setCarried(item);
			}
			
			@Override
			public ItemStack get() {
				return target.containerMenu.getCarried();
			}
		};
		this.personalContents = this.craftingContents = target.inventoryMenu.getCraftSlots().getContents(); //luckily getContents() does not copy
	}
	
	protected MainNmsInventory(Player target, String title) {
		this(target);
		
		this.title = title;
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
		return new MainNmsContainer(containerId, this, playerInventory, viewer);
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
	public Location getLocation() {
		return null;
	}

	@Override
	public int getMaxStackSize() {
		return maxStack;
	}

	@Override
	public InventoryHolder getOwner() {
		return owner;
	}

	@Override
	public List<HumanEntity> getViewers() {
		return transaction;
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
		transaction.remove(bukkitPlayer);
	}

	@Override
	public void onOpen(CraftHumanEntity bukkitPlayer) {
		transaction.add(bukkitPlayer);
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
	public void setMaxStackSize(int amount) {
		this.maxStack = amount;
	}

	@Override
	public boolean stillValid(Player arg0) {
		return true;
	}

}
