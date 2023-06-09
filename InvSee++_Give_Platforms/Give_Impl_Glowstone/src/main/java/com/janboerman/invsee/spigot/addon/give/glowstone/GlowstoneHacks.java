package com.janboerman.invsee.spigot.addon.give.glowstone;

import net.glowstone.util.nbt.CompoundTag;
import net.glowstone.util.nbt.Tag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class GlowstoneHacks {

    private GlowstoneHacks() {
    }

    static void put(CompoundTag tag, String key, Tag value) {
        try {
            Method method = CompoundTag.class.getDeclaredMethod("put", String.class, Tag.class);
            method.setAccessible(true);
            method.invoke(tag, key, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to put child Tag in CompoundTag.", e);
        }
    }

}
