package io.fairyproject.mc.protocol.packet;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;

public interface BufferListener {
    boolean handle(final MCPlayer data, final FairyByteBuf byteBuf);
}
