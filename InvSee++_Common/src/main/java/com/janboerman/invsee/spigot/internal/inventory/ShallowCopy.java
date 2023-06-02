package com.janboerman.invsee.spigot.internal.inventory;

public interface ShallowCopy<Self> {

    //TODO why is this defined in the ShallowCopy interface? I think this needs to be in AbstractNmsInventory.
    //TODO or, we need a new interface.
    public abstract int defaultMaxStack();

    public void shallowCopyFrom(Self from);

}
