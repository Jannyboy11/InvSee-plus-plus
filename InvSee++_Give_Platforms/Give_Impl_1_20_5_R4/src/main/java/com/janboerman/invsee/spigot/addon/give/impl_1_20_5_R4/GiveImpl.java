package com.janboerman.invsee.spigot.addon.give.impl_1_20_5_R4;

import com.janboerman.invsee.spigot.addon.give.common.NeditImpl;
import me.nullicorn.nedit.type.*;
import net.minecraft.nbt.*;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;

import java.util.Map.Entry;

/**
 * @deprecated does not work for the new 'component' architecture of item stacks.
 * Will need a replacement, possibly a change of the command syntax.
 */
@Deprecated
public class GiveImpl extends NeditImpl {

    public static final GiveImpl INSTANCE = new GiveImpl();

    private GiveImpl() {
    }

    @Override
    protected org.bukkit.inventory.ItemStack applyTag(org.bukkit.inventory.ItemStack stack, NBTCompound tag) {
        var nmsStack = CraftItemStack.asNMSCopy(stack);
        CompoundTag nmsTag = convert(tag);
        //nmsStack.setTag(nmsTag); // TODO 1.20.5 set the tag, but how?!
        return CraftItemStack.asCraftMirror(nmsStack);
    }

    // TODO should be writing a converter to components instead, use Vanilla's PatchedDataComponentMap
    // TODO call ItemStack.setComponentsClone(PatchedDataComponentMap).

    private static net.minecraft.nbt.Tag convert(Object o) {
        return switch (o) {
            case null -> EndTag.INSTANCE;
            case Byte b -> ByteTag.valueOf(b);
            case Short s -> ShortTag.valueOf(s);
            case Integer i -> IntTag.valueOf(i);
            case Long l -> LongTag.valueOf(l);
            case Float f -> FloatTag.valueOf(f);
            case Double d -> DoubleTag.valueOf(d);
            case byte[] ba -> new ByteArrayTag(ba);
            case String s -> StringTag.valueOf(s);
            case NBTList list -> convert(list);
            case NBTCompound dict -> convert(dict);
            case int[] ia -> new IntArrayTag(ia);
            case long[] la -> new LongArrayTag(la);
            default -> throw new RuntimeException("Cannot convert " + o + " to its nbt-equivalent");
        };
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
