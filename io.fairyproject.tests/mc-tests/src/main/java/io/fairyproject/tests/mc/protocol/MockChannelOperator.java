/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.tests.mc.protocol;

import com.github.retrooper.packetevents.netty.channel.ChannelOperator;

import java.net.SocketAddress;
import java.util.List;
import java.util.Set;

public class MockChannelOperator implements ChannelOperator {
    @Override
    public Set<Class<? extends Throwable>> getIgnoredHandlerExceptions() {
        return null;
    }

    @Override
    public SocketAddress remoteAddress(Object o) {
        return null;
    }

    @Override
    public SocketAddress localAddress(Object o) {
        return null;
    }

    @Override
    public boolean isOpen(Object o) {
        return false;
    }

    @Override
    public Object close(Object o) {
        return null;
    }

    @Override
    public Object write(Object o, Object o1) {
        return null;
    }

    @Override
    public Object flush(Object o) {
        return null;
    }

    @Override
    public Object writeAndFlush(Object o, Object o1) {
        return null;
    }

    @Override
    public Object fireChannelRead(Object o, Object o1) {
        return null;
    }

    @Override
    public Object writeInContext(Object o, String s, Object o1) {
        return null;
    }

    @Override
    public Object flushInContext(Object o, String s) {
        return null;
    }

    @Override
    public Object writeAndFlushInContext(Object o, String s, Object o1) {
        return null;
    }

    @Override
    public Object fireChannelReadInContext(Object o, String s, Object o1) {
        return null;
    }

    @Override
    public List<String> pipelineHandlerNames(Object o) {
        return null;
    }

    @Override
    public Object getPipelineHandler(Object o, String s) {
        return null;
    }

    @Override
    public Object getPipelineContext(Object o, String s) {
        return null;
    }

    @Override
    public Object getPipeline(Object o) {
        return null;
    }

    @Override
    public void runInEventLoop(Object o, Runnable runnable) {
        // Do nothing
    }

    @Override
    public Object pooledByteBuf(Object o) {
        return null;
    }
}
