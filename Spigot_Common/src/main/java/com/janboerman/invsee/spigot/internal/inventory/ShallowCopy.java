package com.janboerman.invsee.spigot.internal.inventory;

public interface ShallowCopy<Self> {

    public abstract int defaultMaxStack();

    public void shallowCopyFrom(Self from);

}
