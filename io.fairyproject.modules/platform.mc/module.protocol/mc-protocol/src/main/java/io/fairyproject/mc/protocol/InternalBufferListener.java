package io.fairyproject.mc.protocol;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;

public interface InternalBufferListener {
    boolean handle(final MCPlayer data, final FairyByteBuf byteBuf);
}
