package com.janboerman.invsee.glowstone;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

class FakeChannel implements Channel {

    static final Channel INSTANCE = new FakeChannel();


    private final ChannelFuture future;

    FakeChannel() {
        this.future = new FakeChannelFuture(this);
    }

    @Override
    public ChannelId id() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventLoop eventLoop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Channel parent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelConfig config() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isRegistered() {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public ChannelMetadata metadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocketAddress localAddress() {
        return FakeSocketAddress.INSTANCE;
    }

    @Override
    public SocketAddress remoteAddress() {
        return FakeSocketAddress.INSTANCE;
    }

    @Override
    public ChannelFuture closeFuture() {
        return future;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public long bytesBeforeUnwritable() {
        return 0;
    }

    @Override
    public long bytesBeforeWritable() {
        return 0;
    }

    @Override
    public Unsafe unsafe() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelPipeline pipeline() {
        return FakePipeline.INSTANCE;
    }

    @Override
    public ByteBufAllocator alloc() {
        return FakeByteBufAllocator.INSTANCE;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress) {
        return future;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress) {
        return future;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1) {
        return future;
    }

    @Override
    public ChannelFuture disconnect() {
        return future;
    }

    @Override
    public ChannelFuture close() {
        return future;
    }

    @Override
    public ChannelFuture deregister() {
        return future;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise) {
        return future;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise) {
        return future;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
        return future;
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise channelPromise) {
        return future;
    }

    @Override
    public ChannelFuture close(ChannelPromise channelPromise) {
        return future;
    }

    @Override
    public ChannelFuture deregister(ChannelPromise channelPromise) {
        return future;
    }

    @Override
    public Channel read() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture write(Object o) {
        return future;
    }

    @Override
    public ChannelFuture write(Object o, ChannelPromise channelPromise) {
        return future;
    }

    @Override
    public Channel flush() {
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise) {
        return future;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o) {
        return future;
    }

    @Override
    public ChannelPromise newPromise() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return future;
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable throwable) {
        return future;
    }

    @Override
    public ChannelPromise voidPromise() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> attributeKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> attributeKey) {
        return false;
    }

    @Override
    public int compareTo(Channel o) {
        throw new UnsupportedOperationException();
    }
}