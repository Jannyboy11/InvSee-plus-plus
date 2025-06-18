package com.janboerman.invsee.spigot.internal.placeholder;

import com.janboerman.invsee.spigot.api.placeholder.PlaceholderGroup;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.inventory.ItemStack;

public abstract class SimplePlaceholderPalette implements PlaceholderPalette {

    private final String name;

    private final ItemStack inaccessible, armourHelmet, armourChestplate, armourLeggings, armourBoots, offHand, body, saddle, cursor, crafting, anvil, merchant, cartography, enchantingItem, enchantingFuel, grindstone, loom, smithingBase, smithingTemplate, smithingAddition, stonecutter, generic;

    public SimplePlaceholderPalette(String name,

                                    ItemStack inaccessible,
                                    ItemStack armourHelmet,
                                    ItemStack armourChestplate,
                                    ItemStack armourLeggings,
                                    ItemStack armourBoots,
                                    ItemStack offHand,
                                    ItemStack body,
                                    ItemStack saddle,
                                    ItemStack cursor,
                                    ItemStack crafting,
                                    ItemStack anvil,
                                    ItemStack merchant,
                                    ItemStack cartography,
                                    ItemStack enchantingItem,
                                    ItemStack enchantingFuel,
                                    ItemStack grindstone,
                                    ItemStack loom,
                                    ItemStack smithingBase,
                                    ItemStack smithingTemplate,
                                    ItemStack smithingAddition,
                                    ItemStack stonecutter,
                                    ItemStack generic) {
        this.name = name;

        this.inaccessible = inaccessible;
        this.armourHelmet = armourHelmet;
        this.armourChestplate = armourChestplate;
        this.armourLeggings = armourLeggings;
        this.armourBoots = armourBoots;
        this.offHand = offHand;
        this.body = body;
        this.saddle = saddle;
        this.cursor = cursor;
        this.crafting = crafting;
        this.anvil = anvil;
        this.merchant = merchant;
        this.cartography = cartography;
        this.enchantingItem = enchantingItem;
        this.enchantingFuel = enchantingFuel;
        this.grindstone = grindstone;
        this.loom = loom;
        this.smithingBase = smithingBase;
        this.smithingTemplate = smithingTemplate;
        this.smithingAddition = smithingAddition;
        this.stonecutter = stonecutter;
        this.generic = generic;
    }

    @Override public String toString() { return name; }

    @Override public ItemStack inaccessible() { return clone(inaccessible); }
    @Override public ItemStack armourHelmet() { return clone(armourHelmet); }
    @Override public ItemStack armourChestplate() { return clone(armourChestplate); }
    @Override public ItemStack armourLeggings() { return clone(armourLeggings); }
    @Override public ItemStack armourBoots() { return clone(armourBoots); }
    @Override public ItemStack offHand() { return clone(offHand); }
    @Override public ItemStack body() { return clone(body); }
    @Override public ItemStack saddle() { return clone(saddle); }
    @Override public ItemStack cursor() { return clone(cursor); }
    @Override public ItemStack crafting() { return clone(crafting); }
    @Override public ItemStack anvil() { return clone(anvil); }
    @Override public ItemStack merchant() { return clone(merchant); }
    @Override public ItemStack cartography() { return clone(cartography); }
    @Override public ItemStack enchantingItem() { return clone(enchantingItem); }
    @Override public ItemStack enchantingFuel() { return clone(enchantingFuel); }
    @Override public ItemStack grindstone() { return clone(grindstone); }
    @Override public ItemStack loom() { return clone(loom); }
    @Override public ItemStack smithingBase() { return clone(smithingBase); }
    @Override public ItemStack smithingTemplate() { return clone(smithingTemplate); }
    @Override public ItemStack smithingAddition() { return clone(smithingAddition); }
    @Override public ItemStack stonecutter() { return clone(stonecutter); }
    @Override public ItemStack generic() { return clone(generic); }

    @Override
    public abstract ItemStack getPersonalSlotPlaceholder(PlayerInventorySlot slot, PlaceholderGroup placeholderGroup);

    private static ItemStack clone(ItemStack itemStack) {
        return itemStack == null ? null : itemStack.clone();
    }
}
