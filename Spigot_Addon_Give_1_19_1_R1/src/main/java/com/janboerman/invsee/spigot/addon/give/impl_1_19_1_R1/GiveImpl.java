package com.janboerman.invsee.spigot.addon.give.impl_1_19_1_R1;

import com.janboerman.invsee.spigot.addon.give.common.NeditImpl;
import me.nullicorn.nedit.type.*;
import net.minecraft.nbt.*;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;

import java.util.Map.Entry;

public class GiveImpl extends NeditImpl {

    public static final GiveImpl INSTANCE = new GiveImpl();

    private GiveImpl() {
    }

    @Override
    protected org.bukkit.inventory.ItemStack applyTag(org.bukkit.inventory.ItemStack stack, NBTCompound tag) {
        var nmsStack = CraftItemStack.asNMSCopy(stack);
        CompoundTag nmsTag = convert(tag);
        nmsStack.setTag(nmsTag);
        return CraftItemStack.asCraftMirror(nmsStack);
    }

    private static net.minecraft.nbt.Tag convert(Object o) {
        if (o == null) return EndTag.INSTANCE;
        if (o instanceof Byte b) return ByteTag.valueOf(b);
        if (o instanceof Short s) return ShortTag.valueOf(s);
        if (o instanceof Integer i) return IntTag.valueOf(i);
        if (o instanceof Long l) return LongTag.valueOf(l);
        if (o instanceof Float f) return FloatTag.valueOf(f);
        if (o instanceof Double d) return DoubleTag.valueOf(d);
        if (o instanceof byte[] ba) return new ByteArrayTag(ba);
        if (o instanceof String s) return StringTag.valueOf(s);
        if (o instanceof NBTList list) return convert(list);
        if (o instanceof NBTCompound dict) return convert(dict);
        if (o instanceof int[] ia) return new IntArrayTag(ia);
        if (o instanceof long[] la) return new LongArrayTag(la);

        throw new RuntimeException("Cannot convert " + o + " to its nbt-equivalent");
    }

    private static CompoundTag convert(NBTCompound tag) {
        CompoundTag compoundTag = new CompoundTag();
        for (Entry<String, Object> entry : tag.entrySet()) {
            compoundTag.put(entry.getKey(), convert(entry.getValue()));
        }
        return compoundTag;
    }

    private static ListTag convert(NBTList tag) {
        ListTag listTag = new ListTag();
        for (Object o : tag) {
            listTag.add(convert(o));
        }
        return listTag;
    }

}
