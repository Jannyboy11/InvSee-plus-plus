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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;

public class FakeChannel implements Channel {

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
        try {
            return new ServerSocket(25565, 0, InetAddress.getByName(null)).getLocalSocketAddress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SocketAddress remoteAddress() {
        try {
            return new ServerSocket(25565, 0, InetAddress.getByName(null)).getLocalSocketAddress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChannelFuture closeFuture() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBufAllocator alloc() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture disconnect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture deregister() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise channelPromise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture close(ChannelPromise channelPromise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture deregister(ChannelPromise channelPromise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Channel read() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture write(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture write(Object o, ChannelPromise channelPromise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Channel flush() {
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture writeAndFlush(Object o) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable throwable) {
        throw new UnsupportedOperationException();
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
