package com.janboerman.invsee.spigot.internal.inventory;

public interface Wrapper<NMS extends ShallowCopy<NMS>, Self extends Wrapper<NMS, Self>> extends ShallowCopy<Self> {

    public NMS getInventory();

    @Override
    public default void shallowCopyFrom(Self from) {
        getInventory().shallowCopyFrom(from.getInventory());
    }

    @Override
    public default int defaultMaxStack() {
        return getInventory().defaultMaxStack();
    }

}
