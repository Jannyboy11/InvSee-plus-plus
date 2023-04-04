package com.janboerman.invsee.fakes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class FakeItemFactory implements ItemFactory {

    static final FakeItemFactory INSTANCE = new FakeItemFactory();

    private Tag<Material> SPAWN_EGGS = null;

    private FakeItemFactory() {
    }

    private Tag<Material> spawnEggsTag() {
        if (SPAWN_EGGS != null) return SPAWN_EGGS;

        return SPAWN_EGGS = new Tag<Material>() {
            private final NamespacedKey key = new NamespacedKey("InvseePlusPlus", "spawn_eggs");
            private final Set<Material> spawnEggs = Arrays.stream(Material.values())
                    .filter(m -> !m.name().startsWith("LEGACY_"))
                    .filter(m -> m.name().endsWith("SPAWN_EGG"))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));

            @Override
            public boolean isTagged(Material material) {
                return spawnEggs.contains(material);
            }

            @Override
            public Set<Material> getValues() {
                return Collections.unmodifiableSet(spawnEggs);
            }

            @Override
            public NamespacedKey getKey() {
                return key;
            }
        };
    }

    @Override
    public @Nullable ItemStack getSpawnEgg(@Nullable EntityType entityType) {
        return entityType == null ? null : new ItemStack(Material.valueOf(entityType.name() + "_SPAWN_EGG"));
    }

    @Override
    public ItemMeta getItemMeta(Material material) {
        return new FakeItemMeta();
    }

    @Override
    public boolean isApplicable(ItemMeta itemMeta, ItemStack itemStack) throws IllegalArgumentException {
        return isApplicable(itemMeta, itemStack.getType());
    }

    @Override
    public boolean isApplicable(ItemMeta itemMeta, Material material) throws IllegalArgumentException {
        if (itemMeta instanceof BannerMeta) {
            return Tag.ITEMS_BANNERS.isTagged(material);
        } else if (itemMeta instanceof BlockDataMeta) {
            if (Tag.BEDS.isTagged(material)) return true;
            if (Tag.WALL_CORALS.isTagged(material)) return true;
            if (Tag.DOORS.isTagged(material)) return true;
            if (Tag.FENCES.isTagged(material)) return true;
            if (Tag.FENCE_GATES.isTagged(material)) return true;
            if (Tag.LEAVES.isTagged(material)) return true;
            if (Tag.RAILS.isTagged(material)) return true;
            if (Tag.SAPLINGS.isTagged(material)) return true;
            if (Tag.SIGNS.isTagged(material)) return true;
            if (Tag.SLABS.isTagged(material)) return true;
            if (Tag.STAIRS.isTagged(material)) return true;
            if (Tag.TRAPDOORS.isTagged(material)) return true;
            if (Tag.WALLS.isTagged(material)) return true;
            if (Tag.WALL_SIGNS.isTagged(material)) return true;

            switch (material) {
                case BAMBOO_SAPLING:
                case BAMBOO:
                case BEE_NEST:
                case BELL:
                case BREWING_STAND:
                case BUBBLE_COLUMN:
                case CAKE:
                case CAMPFIRE:
                case SOUL_CAMPFIRE:
                case CHAIN:
                case CHEST:
                case TRAPPED_CHEST:
                case CHEST_MINECART:
                case COCOA:
                case COMMAND_BLOCK:
                case COMMAND_BLOCK_MINECART:
                case REPEATING_COMMAND_BLOCK:
                case CHAIN_COMMAND_BLOCK:
                case COMPARATOR:
                case DAYLIGHT_DETECTOR:
                case DISPENSER:
                case DROPPER:
                case ENDER_CHEST:
                case END_PORTAL_FRAME:
                case FARMLAND:
                case FURNACE:
                case BLAST_FURNACE:
                case SMOKER:
                case GRINDSTONE:
                case HOPPER:
                case HOPPER_MINECART:
                case JIGSAW:
                case JUKEBOX:
                case LADDER:
                case LANTERN:
                case LECTERN:
                case NOTE_BLOCK:
                case OBSERVER:
                case PISTON:
                case STICKY_PISTON:
                case PISTON_HEAD:
                case MOVING_PISTON:
                case REDSTONE_WALL_TORCH:
                case REDSTONE_WIRE:
                case REPEATER:
                case RESPAWN_ANCHOR:
                case SCAFFOLDING:
                case SEA_PICKLE:
                case SNOW:
                case STRUCTURE_BLOCK:
                case LEVER:
                case TNT:
                case TNT_MINECART:
                case TRIPWIRE:
                case TRIPWIRE_HOOK:
                case TURTLE_EGG:
                    return true;
            }

            return false;
        } else if (itemMeta instanceof BlockStateMeta) {
            if (Tag.SHULKER_BOXES.isTagged(material)) return true;
            if (Tag.SIGNS.isTagged(material)) return true;
            if (Tag.WALL_SIGNS.isTagged(material)) return true;

            switch (material) {
                case SHIELD:
                case TROPICAL_FISH_BUCKET:
                case BEACON:
                case BEE_NEST:
                case BELL:
                case CAMPFIRE:
                case SOUL_CAMPFIRE:
                case CHEST:
                case COMMAND_BLOCK:
                case COMMAND_BLOCK_MINECART:
                case CHAIN_COMMAND_BLOCK:
                case REPEATING_COMMAND_BLOCK:
                case TRAPPED_CHEST:
                case COMPARATOR:
                case CONDUIT:
                case SPAWNER:
                case DAYLIGHT_DETECTOR:
                case ENCHANTING_TABLE:
                case ENDER_CHEST:
                case END_GATEWAY:
                case JIGSAW:
                case STRUCTURE_BLOCK:
                case DISPENSER:
                case DROPPER:
                case BREWING_STAND:
                case FURNACE:
                case BLAST_FURNACE:
                case SMOKER:
                case BARREL:
                case HOPPER:
                case LECTERN:
                case SHULKER_BOX:   //not sure if the un-dyed shulkerbox is included in the SHULKER_BOXES tag.
                case JUKEBOX:
                    return true;
            }

            return false;
        } else if (itemMeta instanceof BookMeta) {
            return material == Material.WRITTEN_BOOK || material == Material.WRITABLE_BOOK;
        } else if (itemMeta instanceof CompassMeta) {
            return material == Material.COMPASS;
        } else if (itemMeta instanceof CrossbowMeta) {
            return material == Material.CROSSBOW;
        } else if (itemMeta instanceof EnchantmentStorageMeta) {
            return material == Material.ENCHANTED_BOOK;
        } else if (itemMeta instanceof FireworkEffectMeta) {
            return material == Material.FIREWORK_STAR;
        } else if (itemMeta instanceof FireworkMeta) {
            return material == Material.FIREWORK_ROCKET;
        } else if (itemMeta instanceof LeatherArmorMeta) {
            switch (material) {
                case LEATHER_BOOTS:
                case LEATHER_LEGGINGS:
                case LEATHER_CHESTPLATE:
                case LEATHER_HELMET:
                case LEATHER_HORSE_ARMOR:
                    return true;
                default:
                    return false;
            }
        } else if (itemMeta instanceof MapMeta) {
            return material == Material.FILLED_MAP;
        } else if (itemMeta instanceof PotionMeta) {
            switch (material) {
                case POTION:
                case LINGERING_POTION:
                case SPLASH_POTION:
                case TIPPED_ARROW:
                    return true;
                default:
                    return false;
            }
        } else if (itemMeta instanceof SkullMeta) {
            switch (material) {
                case PLAYER_HEAD:
                case PLAYER_WALL_HEAD:
                case CREEPER_HEAD:
                case CREEPER_WALL_HEAD:
                case ZOMBIE_HEAD:
                case ZOMBIE_WALL_HEAD:
                case SKELETON_SKULL:
                case SKELETON_WALL_SKULL:
                case WITHER_SKELETON_SKULL:
                case WITHER_SKELETON_WALL_SKULL:
                case DRAGON_HEAD:
                case DRAGON_WALL_HEAD:
                    return true;
                default:
                    return false;
            }
        } else if (itemMeta instanceof SpawnEggMeta) {
            return spawnEggsTag().isTagged(material);
        } else if (itemMeta instanceof SuspiciousStewMeta) {
            return material == Material.SUSPICIOUS_STEW;
        } else if (itemMeta instanceof TropicalFishBucketMeta) {
            //does not apply for cod, salmon, pufferfish.
            return material == Material.TROPICAL_FISH_BUCKET;
        }

        //standard ItemMeta always applies
        return true;
    }

    @Override
    public boolean equals(ItemMeta itemMeta, ItemMeta itemMeta1) throws IllegalArgumentException {
        if (itemMeta == null) return itemMeta1 == null || new FakeItemMeta().equals(itemMeta1);
        if (itemMeta1 == null) return new FakeItemMeta().equals(itemMeta);

        return itemMeta.equals(itemMeta1);
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta itemMeta, ItemStack itemStack) throws IllegalArgumentException {
        return asMetaFor(itemMeta, itemStack.getType());
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta itemMeta, Material material) throws IllegalArgumentException {
        return itemMeta;
    }

    @Override
    public Color getDefaultLeatherColor() {
        return Color.fromRGB(0xA06540);
    }

    @Override
    public @NotNull ItemStack createItemStack(@NotNull String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException(); //this is probably dual of ItemMeta#getAsString()
    }

    @Override
    public Material updateMaterial(ItemMeta itemMeta, Material material) throws IllegalArgumentException {
        return material;
    }

    @Override
    public @NotNull ItemStack enchantWithLevels(@NotNull ItemStack itemStack, @Range(from = 1L, to = 30L) int i, boolean b, @NotNull Random random) {
        throw new UnsupportedOperationException(); //TODO could probably implement this, right?
    }

    @Override
    public @NotNull HoverEvent<ShowItem> asHoverEvent(@NotNull ItemStack itemStack, @NotNull UnaryOperator<ShowItem> unaryOperator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Component displayName(@NotNull ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack ensureServerConversions(ItemStack itemStack) {
        return itemStack;
    }

    @Override
    public String getI18NDisplayName(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Content hoverContentOf(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Content hoverContentOf(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Content hoverContentOf(Entity entity, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Content hoverContentOf(Entity entity, BaseComponent baseComponent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Content hoverContentOf(Entity entity, BaseComponent[] baseComponents) {
        throw new UnsupportedOperationException();
    }

}
