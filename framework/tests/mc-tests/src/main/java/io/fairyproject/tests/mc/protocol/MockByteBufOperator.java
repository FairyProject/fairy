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

import com.github.retrooper.packetevents.netty.buffer.ByteBufOperator;

import java.nio.charset.Charset;

public class MockByteBufOperator implements ByteBufOperator {
    @Override
    public int capacity(Object o) {
        return 0;
    }

    @Override
    public Object capacity(Object o, int i) {
        return null;
    }

    @Override
    public int readerIndex(Object o) {
        return 0;
    }

    @Override
    public Object readerIndex(Object o, int i) {
        return null;
    }

    @Override
    public int writerIndex(Object o) {
        return 0;
    }

    @Override
    public Object writerIndex(Object o, int i) {
        return null;
    }

    @Override
    public int readableBytes(Object o) {
        return 0;
    }

    @Override
    public int writableBytes(Object o) {
        return 0;
    }

    @Override
    public Object clear(Object o) {
        return null;
    }

    @Override
    public byte readByte(Object o) {
        return 0;
    }

    @Override
    public short readShort(Object o) {
        return 0;
    }

    @Override
    public int readInt(Object o) {
        return 0;
    }

    @Override
    public long readUnsignedInt(Object o) {
        return 0;
    }

    @Override
    public long readLong(Object o) {
        return 0;
    }

    @Override
    public void writeByte(Object o, int i) {
        // do nothing
    }

    @Override
    public void writeShort(Object o, int i) {
        // do nothing
    }

    @Override
    public void writeInt(Object o, int i) {
        // do nothing
    }

    @Override
    public void writeLong(Object o, long l) {
        // do nothing
    }

    @Override
    public Object getBytes(Object o, int i, byte[] bytes) {
        return null;
    }

    @Override
    public short getUnsignedByte(Object o, int i) {
        return 0;
    }

    @Override
    public boolean isReadable(Object o) {
        return false;
    }

    @Override
    public Object copy(Object o) {
        return null;
    }

    @Override
    public Object duplicate(Object o) {
        return null;
    }

    @Override
    public boolean hasArray(Object o) {
        return false;
    }

    @Override
    public byte[] array(Object o) {
        return new byte[0];
    }

    @Override
    public Object retain(Object o) {
        return null;
    }

    @Override
    public Object retainedDuplicate(Object o) {
        return null;
    }

    @Override
    public Object readSlice(Object o, int i) {
        return null;
    }

    @Override
    public Object readBytes(Object o, byte[] bytes, int i, int i1) {
        return null;
    }

    @Override
    public Object readBytes(Object o, int i) {
        return null;
    }

    @Override
    public void readBytes(Object o, byte[] bytes) {
        // do nothing
    }

    @Override
    public Object writeBytes(Object o, Object o1) {
        return null;
    }

    @Override
    public Object writeBytes(Object o, byte[] bytes) {
        return null;
    }

    @Override
    public Object writeBytes(Object o, byte[] bytes, int i, int i1) {
        return null;
    }

    @Override
    public boolean release(Object o) {
        return false;
    }

    @Override
    public int refCnt(Object o) {
        return 0;
    }

    @Override
    public Object skipBytes(Object o, int i) {
        return null;
    }

    @Override
    public String toString(Object o, int i, int i1, Charset charset) {
        return null;
    }

    @Override
    public Object markReaderIndex(Object o) {
        return null;
    }

    @Override
    public Object resetReaderIndex(Object o) {
        return null;
    }

    @Override
    public Object markWriterIndex(Object o) {
        return null;
    }

    @Override
    public Object resetWriterIndex(Object o) {
        return null;
    }
}
