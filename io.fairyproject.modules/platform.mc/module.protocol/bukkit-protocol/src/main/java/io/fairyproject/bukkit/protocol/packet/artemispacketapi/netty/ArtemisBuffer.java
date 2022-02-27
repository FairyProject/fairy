package io.fairyproject.bukkit.protocol.packet.artemispacketapi.netty;

import cc.ghast.packet.wrapper.netty.MutableByteBuf;
import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;

public class ArtemisBuffer implements FairyByteBuf {
    private final MutableByteBuf byteBuf;

    public ArtemisBuffer(MutableByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public int getSize() {
        return byteBuf.readableBytes();
    }

    @Override
    public byte[] getData() {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        int readerIndex = byteBuf.readerIndex();
        byteBuf.getBytes(readerIndex, bytes);

        return bytes;
    }

    @Override
    public int getReferenceCount() {
        return byteBuf.refCnt();
    }

    @Override
    public boolean isReadable() {
        return byteBuf.isReadable();
    }

    @Override
    public boolean isWriteable() {
        return byteBuf.isWritable();
    }

    @Override
    public int getReaderIndex() {
        return byteBuf.readerIndex();
    }

    @Override
    public void release() {
        byteBuf.release();
    }

    @Override
    public void clear() {
        byteBuf.clear();
    }
}
