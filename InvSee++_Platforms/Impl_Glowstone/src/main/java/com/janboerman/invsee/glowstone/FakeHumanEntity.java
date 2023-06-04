package com.janboerman.invsee.glowstone;

import net.glowstone.entity.GlowHumanEntity;
import net.glowstone.entity.meta.profile.GlowPlayerProfile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;

import java.util.EnumMap;

class FakeHumanEntity extends GlowHumanEntity {

    private MainHand mainHand = MainHand.RIGHT;
    private EnumMap<Material, Integer> cooldowns = new EnumMap<>(Material.class);

    FakeHumanEntity(Location location, GlowPlayerProfile profile) {
        super(location, profile);
    }

    @Override
    public MainHand getMainHand() {
        return mainHand;
    }

    @Override
    public InventoryView openMerchant(Villager villager, boolean force) {
        return null;
    }

    @Override
    public InventoryView openMerchant(Merchant merchant, boolean force) {
        return null;
    }

    @Override
    public boolean hasCooldown(Material material) {
        return cooldowns.containsKey(material);
    }

    @Override
    public int getCooldown(Material material) {
        Integer cooldown = cooldowns.get(material);
        return cooldown == null ? 0 : cooldown.intValue();
    }

    @Override
    public void setCooldown(Material material, int cooldown) {
        cooldowns.put(material, cooldown);
    }

}
