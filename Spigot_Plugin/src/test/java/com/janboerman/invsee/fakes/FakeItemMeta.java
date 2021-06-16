package com.janboerman.invsee.fakes;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.janboerman.invsee.spigot.internal.FakePersistentDataContainer;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SerializableAs("ItemMeta")
public class FakeItemMeta implements ItemMeta, Damageable {

    private String displayName;
    private String localizedName;
    private List<String> lore;
    private Integer customModelData;
    private Map<Enchantment, Integer> enchants;
    private Set<ItemFlag> flags;
    private boolean unbreakable;
    private Multimap<Attribute, AttributeModifier> attributes;
    private int version;
    private FakePersistentDataContainer persistentData;
    private int damage;

    @Override
    public boolean hasDisplayName() {
        return displayName != null;
    }

    @Override
    public @Nullable Component displayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void displayName(@Nullable Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public BaseComponent[] getDisplayNameComponent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDisplayName(String s) {
        this.displayName = s;
    }

    @Override
    public void setDisplayNameComponent(BaseComponent[] baseComponents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasLocalizedName() {
        return localizedName != null;
    }

    @Override
    public String getLocalizedName() {
        return localizedName;
    }

    @Override
    public void setLocalizedName(String s) {
        this.localizedName = s;
    }

    @Override
    public boolean hasLore() {
        return lore != null;
    }

    @Override
    public @Nullable List<Component> lore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lore(@Nullable List<Component> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getLore() {
        return lore;
    }

    @Override
    public List<BaseComponent[]> getLoreComponents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLore(List<String> list) {
        this.lore = list;
    }

    @Override
    public void setLoreComponents(List<BaseComponent[]> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCustomModelData() {
        return customModelData != null;
    }

    @Override
    public int getCustomModelData() {
        if (customModelData == null) return -1; //i don't know.
        return customModelData.intValue();
    }

    @Override
    public void setCustomModelData(Integer integer) {
        this.customModelData = integer;
    }

    @Override
    public boolean hasEnchants() {
        return enchants != null && !enchants.isEmpty();
    }

    @Override
    public boolean hasEnchant(Enchantment enchantment) {
        return enchants != null && enchants.containsKey(enchantment);
    }

    @Override
    public int getEnchantLevel(Enchantment enchantment) {
        return enchants != null ? enchants.getOrDefault(enchantment, 0) : 0;
    }

    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return enchants;
    }

    @Override
    public boolean addEnchant(Enchantment enchantment, int i, boolean b) {
        if (this.enchants == null) this.enchants = new HashMap<>();

        if (b) {
            return !Objects.equals(enchants.put(enchantment, i), Integer.valueOf(i));
        } else {
            return enchants.putIfAbsent(enchantment, i) == null;
        }
    }

    @Override
    public boolean removeEnchant(Enchantment enchantment) {
        if (this.enchants == null) return false;

        return enchants.remove(enchantment) != null;
    }

    @Override
    public boolean hasConflictingEnchant(Enchantment enchantment) {
        if (this.enchants == null) return false;

        return enchants.keySet().stream().anyMatch(enchantment::conflictsWith);
    }

    @Override
    public void addItemFlags(ItemFlag... itemFlags) {
        if (this.flags == null) this.flags = EnumSet.noneOf(ItemFlag.class);
        flags.addAll(Arrays.asList(itemFlags));
    }

    @Override
    public void removeItemFlags(ItemFlag... itemFlags) {
        if (this.flags != null)
            flags.removeAll(Arrays.asList(itemFlags));
    }

    @Override
    public Set<ItemFlag> getItemFlags() {
        if (this.flags == null) this.flags = EnumSet.noneOf(ItemFlag.class);
        return flags;
    }

    @Override
    public boolean hasItemFlag(ItemFlag itemFlag) {
        return flags != null && flags.contains(itemFlag);
    }

    @Override
    public boolean isUnbreakable() {
        return unbreakable;
    }

    @Override
    public void setUnbreakable(boolean b) {
        this.unbreakable = b;
    }

    @Override
    public boolean hasAttributeModifiers() {
        return attributes != null && !attributes.isEmpty();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        if (attributes == null) attributes = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

        return attributes;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> result = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

        if (attributes != null) {
            attributes.forEach((att, modifier) -> {
                if (Objects.equals(modifier.getSlot(), equipmentSlot)) {
                    result.put(att, modifier);
                }
            });
        }

        return result;
    }

    @Override
    public Collection<AttributeModifier> getAttributeModifiers(Attribute attribute) {
        if (attributes != null) return attributes.asMap().getOrDefault(attribute, Collections.emptyList());
        return Collections.emptyList();

    }

    @Override
    public boolean addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        if (attributes == null) attributes = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

        return attributes.put(attribute, attributeModifier);
    }

    @Override
    public void setAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {
        if (attributes == null) attributes = multimap;
        else attributes.putAll(multimap);
    }

    @Override
    public boolean removeAttributeModifier(Attribute attribute) {
        if (attributes == null) return false;
        Collection<AttributeModifier> modifiers = attributes.removeAll(attribute);
        return modifiers != null && !modifiers.isEmpty();
    }

    @Override
    public boolean removeAttributeModifier(EquipmentSlot equipmentSlot) {
        return attributes.values().removeIf(modifier -> Objects.equals(modifier.getSlot(), equipmentSlot));
    }

    @Override
    public boolean removeAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        return attributes.remove(attribute, attributeModifier);
    }

    @Override
    public CustomItemTagContainer getCustomTagContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVersion(int i) {
       this.version = i;
    }

    @Override
    public Set<Material> getCanDestroy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCanDestroy(Set<Material> set) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Material> getCanPlaceOn() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCanPlaceOn(Set<Material> set) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Namespaced> getDestroyableKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDestroyableKeys(Collection<Namespaced> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Namespaced> getPlaceableKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlaceableKeys(Collection<Namespaced> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPlaceableKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasDestroyableKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        if (persistentData == null) persistentData = new FakePersistentDataContainer();
        return persistentData;
    }

    @Override
    public boolean hasDamage() {
        return damage != 0;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public void setDamage(int i) {
        this.damage = i;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

                                        map.put("version", version);
        if (displayName != null)        map.put("display-name", displayName);
        if (attributes != null)         map.put("attributes", attributes.asMap());
        if (lore != null)               map.put("lore", lore);
        if (flags != null)              map.put("item-flags", flags);
        if (enchants != null)           map.put("enchants", enchants);
        if (localizedName != null)      map.put("localized-name", localizedName);
        if (customModelData != null)    map.put("custom-model-data", customModelData);
        if (unbreakable)                map.put("unbreakable", true);
        if (damage != 0)                map.put("damage", damage);
        //TODO persistent data.
        //TODO if I really want to do this properly, then I should depend on an NBT library.

        return map;
    }

    public static FakeItemMeta deserialize(Map<String, Object> map) {
        FakeItemMeta fakeItemMeta = new FakeItemMeta();

        fakeItemMeta.version = (Integer) map.get("version");
        Object displayName = map.get("display-name"); if (displayName instanceof String) fakeItemMeta.displayName = (String) displayName;
        Object attributes = map.get("attributes"); if (attributes instanceof Map) fakeItemMeta.attributes = Multimaps.newListMultimap((Map) attributes, ArrayList::new);
        Object lore = map.get("lore"); if (lore instanceof List) fakeItemMeta.lore = (List) lore;
        Object flags = map.get("item-flags"); if (flags instanceof Set) fakeItemMeta.flags = (Set) flags;
        Object enchants = map.get("enchants"); if (enchants instanceof Map) fakeItemMeta.enchants = (Map) enchants;
        Object localizedName = map.get("localized-name"); if (localizedName instanceof String) fakeItemMeta.localizedName = (String) localizedName;
        Object customModelData = map.get("custom-model-data"); if (customModelData instanceof Integer) fakeItemMeta.customModelData = (Integer) customModelData;
        Object unbreakable = map.get("unbreakable"); if (unbreakable instanceof Boolean) fakeItemMeta.unbreakable = (Boolean) unbreakable;
        Object damage = map.get("damage"); if (damage instanceof Integer) fakeItemMeta.damage = (Integer) damage;
        //TODO persistent data

        return fakeItemMeta;
    }

    @Override
    public FakeItemMeta clone() {
        FakeItemMeta result = new FakeItemMeta();
        result.displayName = displayName;
        result.attributes = attributes;     //TODO deepcopy
        result.lore = lore;                 //TODO deepcopy
        result.flags = flags;               //TODO deepcopy
        result.enchants = enchants;         //TODO deepcopy
        result.localizedName = localizedName;
        result.customModelData = customModelData;
        result.unbreakable = unbreakable;
        result.version = version;
        result.persistentData = persistentData; //TODO deepcopy
        result.damage = damage;

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ItemMeta)) return false;

        ItemMeta that = (ItemMeta) obj;
        return Objects.equals(this.getDisplayName(), that.getDisplayName())
                && Objects.equals(this.getAttributeModifiers(), that.getAttributeModifiers())
                && Objects.equals(this.getLore(), that.getLore())
                && Objects.equals(this.getItemFlags(), that.getItemFlags())
                && Objects.equals(this.getLocalizedName(), that.getLocalizedName())
                && (this.hasCustomModelData() != that.hasCustomModelData() || this.getCustomModelData() == that.getCustomModelData())
                && (this.isUnbreakable() == that.isUnbreakable())
                && Objects.equals(this.getPersistentDataContainer(), that.getPersistentDataContainer())
                && (!(that instanceof Damageable) || (this.getDamage() == ((Damageable) that).getDamage()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName(),
                getAttributeModifiers(),
                getLore(),
                getItemFlags(),
                getLocalizedName(),
                hasCustomModelData() ? getCustomModelData() : null,
                isUnbreakable(),
                getPersistentDataContainer(),
                getDamage());
    }
}
