package com.janboerman.invsee.glowstone;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class FakePipeline implements ChannelPipeline {

    static ChannelPipeline INSTANCE = new FakePipeline();
    private static ChannelFuture FUTURE = new FakeChannelFuture(FakeChannel.INSTANCE);

    private FakePipeline() {}


    @Override
    public ChannelPipeline addFirst(String s, ChannelHandler channelHandler) {
        return this;
    }

    @Override
    public ChannelPipeline addFirst(EventExecutorGroup eventExecutorGroup, String s, ChannelHandler channelHandler) {
        return this;
    }

    @Override
    public ChannelPipeline addLast(String s, ChannelHandler channelHandler) {
        return this;
    }

    @Override
    public ChannelPipeline addLast(EventExecutorGroup eventExecutorGroup, String s, ChannelHandler channelHandler) {
        return this;
    }

    @Override
    public ChannelPipeline addBefore(String s, String s1, ChannelHandler channelHandler) {
        return this;
    }

    @Override
    public ChannelPipeline addBefore(EventExecutorGroup eventExecutorGroup, String s, String s1, ChannelHandler channelHandler) {
        return this;
    }

    @Override
    public ChannelPipeline addAfter(String s, String s1, ChannelHandler channelHandler) {
        return this;
    }

    @Override
    public ChannelPipeline addAfter(EventExecutorGroup eventExecutorGroup, String s, String s1, ChannelHandler channelHandler) {
        return this;
    }

    @Override
    public ChannelPipeline addFirst(ChannelHandler... channelHandlers) {
        return this;
    }

    @Override
    public ChannelPipeline addFirst(EventExecutorGroup eventExecutorGroup, ChannelHandler... channelHandlers) {
        return this;
    }

    @Override
    public ChannelPipeline addLast(ChannelHandler... channelHandlers) {
        return this;
    }

    @Override
    public ChannelPipeline addLast(EventExecutorGroup eventExecutorGroup, ChannelHandler... channelHandlers) {
        return this;
    }

    @Override
    public ChannelPipeline remove(ChannelHandler channelHandler) {
        return this;
    }

    @Override
    public ChannelHandler remove(String s) {
        return null;
    }

    @Override
    public <T extends ChannelHandler> T remove(Class<T> aClass) {
        return null;
    }

    @Override
    public ChannelHandler removeFirst() {
        return null;
    }

    @Override
    public ChannelHandler removeLast() {
        return null;
    }

    @Override
    public ChannelPipeline replace(ChannelHandler channelHandler, String s, ChannelHandler channelHandler1) {
        return null;
    }

    @Override
    public ChannelHandler replace(String s, String s1, ChannelHandler channelHandler) {
        return null;
    }

    @Override
    public <T extends ChannelHandler> T replace(Class<T> aClass, String s, ChannelHandler channelHandler) {
        return null;
    }

    @Override
    public ChannelHandler first() {
        return null;
    }

    @Override
    public ChannelHandlerContext firstContext() {
        return null;
    }

    @Override
    public ChannelHandler last() {
        return null;
    }

    @Override
    public ChannelHandlerContext lastContext() {
        return null;
    }

    @Override
    public ChannelHandler get(String s) {
        return null;
    }

    @Override
    public <T extends ChannelHandler> T get(Class<T> aClass) {
        return null;
    }

    @Override
    public ChannelHandlerContext context(ChannelHandler channelHandler) {
        return null;
    }

    @Override
    public ChannelHandlerContext context(String s) {
        return null;
    }

    @Override
    public ChannelHandlerContext context(Class<? extends ChannelHandler> aClass) {
        return null;
    }

    @Override
    public Channel channel() {
        return FakeChannel.INSTANCE;
    }

    @Override
    public List<String> names() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, ChannelHandler> toMap() {
        return Collections.emptyMap();
    }

    @Override
    public ChannelPipeline fireChannelRegistered() {
        return this;
    }

    @Override
    public ChannelPipeline fireChannelUnregistered() {
        return this;
    }

    @Override
    public ChannelPipeline fireChannelActive() {
        return this;
    }

    @Override
    public ChannelPipeline fireChannelInactive() {
        return this;
    }

    @Override
    public ChannelPipeline fireExceptionCaught(Throwable throwable) {
        return this;
    }

    @Override
    public ChannelPipeline fireUserEventTriggered(Object o) {
        return this;
    }

    @Override
    public ChannelPipeline fireChannelRead(Object o) {
        return this;
    }

    @Override
    public ChannelPipeline fireChannelReadComplete() {
        return this;
    }

    @Override
    public ChannelPipeline fireChannelWritabilityChanged() {
        return this;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress) {
        return FUTURE;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress) {
        return FUTURE;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1) {
        return FUTURE;
    }

    @Override
    public ChannelFuture disconnect() {
        return FUTURE;
    }

    @Override
    public ChannelFuture close() {
        return FUTURE;
    }

    @Override
    public ChannelFuture deregister() {
        return FUTURE;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise) {
        return FUTURE;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise) {
        return FUTURE;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
        return FUTURE;
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise channelPromise) {
        return FUTURE;
    }

    @Override
    public ChannelFuture close(ChannelPromise channelPromise) {
        return FUTURE;
    }

    @Override
    public ChannelFuture deregister(ChannelPromise channelPromise) {
        return FUTURE;
    }

    @Override
    public ChannelOutboundInvoker read() {
        return null;
    }

    @Override
    public ChannelFuture write(Object o) {
        return FUTURE;
    }

    @Override
    public ChannelFuture write(Object o, ChannelPromise channelPromise) {
        return FUTURE;
    }

    @Override
    public ChannelPipeline flush() {
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise) {
        return FUTURE;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o) {
        return FUTURE;
    }

    @Override
    public ChannelPromise newPromise() {
        return null;
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return null;
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return FUTURE;
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable throwable) {
        return FUTURE;
    }

    @Override
    public ChannelPromise voidPromise() {
        return null;
    }

    @Override
    public Iterator<Entry<String, ChannelHandler>> iterator() {
        return Collections.emptyIterator();
    }
}
