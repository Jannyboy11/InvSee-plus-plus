package com.janboerman.invsee.spigot.addon.give.glowstone;

import com.janboerman.invsee.spigot.addon.give.common.NeditImpl;
import me.nullicorn.nedit.type.*;
import net.glowstone.inventory.GlowItemFactory;
import net.glowstone.util.nbt.*;
import net.glowstone.util.nbt.TagType;
import org.bukkit.inventory.ItemStack;

import java.util.Map.Entry;
import java.util.stream.Collectors;

public class GiveImpl extends NeditImpl {

    public static final GiveImpl INSTANCE = new GiveImpl();

    private GiveImpl() {
    }

    @Override
    protected ItemStack applyTag(ItemStack stack, NBTCompound tag) {
        stack.setItemMeta(GlowItemFactory.instance().readNbt(stack.getType(), convert(tag)));
        return stack;
    }

    private static net.glowstone.util.nbt.Tag convert(Object o) {
        if (o == null) return null;
        if (o instanceof Byte) return new ByteTag((Byte) o);
        if (o instanceof Short) return new ShortTag((Short) o);
        if (o instanceof Integer) return new IntTag((Integer) o);
        if (o instanceof Long) return new LongTag((Long) o);
        if (o instanceof Float) return new FloatTag((Float) o);
        if (o instanceof Double) return new DoubleTag((Double) o);
        if (o instanceof byte[]) return new ByteArrayTag((byte[]) o);
        if (o instanceof String) return new StringTag((String) o);
        if (o instanceof NBTList) return convert((NBTList) o);
        if (o instanceof NBTCompound) return convert((NBTCompound) o);
        if (o instanceof int[]) return new IntArrayTag((int[]) o);
        if (o instanceof long[]) throw new UnsupportedOperationException("Long_Array NBT Tag type unsupported on Glowstone!");

        throw new UnsupportedOperationException("Cannot convert " + o + " to its nbt-equivalent");
    }

    private static CompoundTag convert(NBTCompound tag) {
        CompoundTag compoundTag = new CompoundTag();
        for (Entry<String, Object> entry : tag.entrySet()) {
            GlowstoneHacks.put(compoundTag, entry.getKey(), convert(entry.getValue()));
        }
        return compoundTag;
    }

    private static ListTag convert(NBTList tag) {
        return new ListTag(TagType.byId(tag.getContentType().getId()),
                tag.stream()
                    .map(GiveImpl::convert)
                    .collect(Collectors.toList()));
    }

    @Override
    public int maxStackSize() {
        // There does not seem to be any field or method in Glowstone to ge tthis value.
        return 64;
    }

}
