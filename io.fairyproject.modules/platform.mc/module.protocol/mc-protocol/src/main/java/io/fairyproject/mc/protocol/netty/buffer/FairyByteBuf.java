package io.fairyproject.mc.protocol.netty.buffer;

public interface FairyByteBuf {
    int getSize();

    int getReferenceCount();

    boolean isReadable();

    boolean isWriteable();

    int getReaderIndex();

    byte[] getData();

    void release();

    void clear();
}
