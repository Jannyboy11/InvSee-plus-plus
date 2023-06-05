package com.janboerman.invsee.glowstone;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.EmptyByteBuf;

class FakeByteBufAllocator implements ByteBufAllocator {

    static final ByteBufAllocator INSTANCE = new FakeByteBufAllocator();

    private final EmptyByteBuf buffer;
    private final CompositeByteBuf composite;

    private FakeByteBufAllocator() {
        this.buffer = new EmptyByteBuf(INSTANCE);
        this.composite = new CompositeByteBuf(this, true, 0);
    }

    @Override
    public ByteBuf buffer() {
        return buffer;
    }

    @Override
    public ByteBuf buffer(int i) {
        return buffer;
    }

    @Override
    public ByteBuf buffer(int i, int i1) {
        return buffer;
    }

    @Override
    public ByteBuf ioBuffer() {
        return buffer;
    }

    @Override
    public ByteBuf ioBuffer(int i) {
        return buffer;
    }

    @Override
    public ByteBuf ioBuffer(int i, int i1) {
        return buffer;
    }

    @Override
    public ByteBuf heapBuffer() {
        return buffer;
    }

    @Override
    public ByteBuf heapBuffer(int i) {
        return buffer;
    }

    @Override
    public ByteBuf heapBuffer(int i, int i1) {
        return buffer;
    }

    @Override
    public ByteBuf directBuffer() {
        return buffer;
    }

    @Override
    public ByteBuf directBuffer(int i) {
        return buffer;
    }

    @Override
    public ByteBuf directBuffer(int i, int i1) {
        return buffer;
    }

    @Override
    public CompositeByteBuf compositeBuffer() {
        return composite;
    }

    @Override
    public CompositeByteBuf compositeBuffer(int i) {
        return composite;
    }

    @Override
    public CompositeByteBuf compositeHeapBuffer() {
        return composite;
    }

    @Override
    public CompositeByteBuf compositeHeapBuffer(int i) {
        return composite;
    }

    @Override
    public CompositeByteBuf compositeDirectBuffer() {
        return composite;
    }

    @Override
    public CompositeByteBuf compositeDirectBuffer(int i) {
        return composite;
    }

    @Override
    public boolean isDirectBufferPooled() {
        return false;
    }

    @Override
    public int calculateNewCapacity(int i, int i1) {
        return 0;
    }
}
