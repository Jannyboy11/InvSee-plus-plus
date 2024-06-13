package com.janboerman.invsee.spigot.perworldinventory;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FakeAttributeInstance implements AttributeInstance {

    private final Attribute attribute;
    private double baseValue;
    private final Collection<AttributeModifier> modifiers = new TreeSet<>(Comparator
            .comparing(AttributeModifier::getOperation)
            .thenComparing(AttributeModifier::getUniqueId));

    public FakeAttributeInstance(Attribute attribute) {
        this.attribute = attribute;
        this.baseValue = getDefaultValue();
    }

    @NotNull
    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double v) {
        baseValue = v;
    }

    @NotNull
    @Override
    public Collection<AttributeModifier> getModifiers() {
        return modifiers;
    }

    @Override
    public void addModifier(@NotNull AttributeModifier attributeModifier) {
        modifiers.add(attributeModifier);
    }

    @Override
    public void removeModifier(@NotNull AttributeModifier attributeModifier) {
        modifiers.remove(attributeModifier);
    }

    @Override
    public double getValue() {
        //https://minecraft.wiki/w/Attribute#Operations

        double x = getBaseValue();
        double y = -1;

        AttributeModifier.Operation lastOperation = null;
        for (AttributeModifier modifier : getModifiers()) {
            //gotta love my custom iteration order :)
            AttributeModifier.Operation operation = modifier.getOperation();
            if (lastOperation == AttributeModifier.Operation.ADD_NUMBER && operation == AttributeModifier.Operation.ADD_SCALAR) {
                y = x;
            }
            double byAmount = modifier.getAmount();
            switch (operation) {
                case ADD_NUMBER:
                    x += byAmount;
                    break;
                case ADD_SCALAR:
                    y += x*byAmount;
                    break;
                case MULTIPLY_SCALAR_1:
                    y = y * (1 + byAmount); //y += y*byAmount
                    break;
            }
            lastOperation = operation;
        }

        switch (getAttribute()) {
            case GENERIC_ATTACK_KNOCKBACK:
                y = between(0, y, 8);
            case GENERIC_ARMOR:
                y = between(0, y, 30);
                break;
            case GENERIC_ARMOR_TOUGHNESS:
                y = between(0, y, 20);
                break;
            case GENERIC_ATTACK_DAMAGE:
                y = between(0, y, 2048);
                break;
            case GENERIC_ATTACK_SPEED:
                y = between(0, y, 1024);
                break;
            case GENERIC_FLYING_SPEED:
                y = between(0, y, 1024);
                break;
            case GENERIC_FOLLOW_RANGE:
                y = between(0, y, 2048);
                break;
            case GENERIC_KNOCKBACK_RESISTANCE:
                y = between(0, y, 1);
                break;
            case GENERIC_LUCK:
                y = between(-1024, y, 1024);
                break;
            case GENERIC_MAX_HEALTH:
                y = between(0, y, 1024);
                break;
            case GENERIC_MOVEMENT_SPEED:
                y = between(0, y, 1024);
                break;

            case HORSE_JUMP_STRENGTH:
                y = between(0, y, 2);
                break;
            case ZOMBIE_SPAWN_REINFORCEMENTS:
                y = between(0, y, 1);
                break;

            default:
                y = between(Double.MIN_NORMAL, y, Double.MAX_VALUE);
        }

        return y;
    }

    @Override
    public double getDefaultValue() {
        switch (getAttribute()) {
            case GENERIC_MAX_HEALTH:
                return 20;
            case GENERIC_FOLLOW_RANGE:
                return 32;
            case GENERIC_KNOCKBACK_RESISTANCE:
                return 0;
            case GENERIC_MOVEMENT_SPEED:
                return 0.7;
            case GENERIC_ATTACK_DAMAGE:
                return 1;   //2.0 * half hearts
            case GENERIC_ARMOR:
                return 0;
            case GENERIC_ARMOR_TOUGHNESS:
                return 0;
            case GENERIC_ATTACK_SPEED:
                return 4;
            case GENERIC_LUCK:
                return 0;
            case HORSE_JUMP_STRENGTH:
                return 0.7;
            case GENERIC_FLYING_SPEED:
                return 0.4;
            case ZOMBIE_SPAWN_REINFORCEMENTS:
                return 0;
            case GENERIC_ATTACK_KNOCKBACK:
                return 0;
            default:
                return 0;
        }
    }

    private static double between(double min, double value, double max) {
        return Math.min(Math.max(min, value), max);
    }
}
