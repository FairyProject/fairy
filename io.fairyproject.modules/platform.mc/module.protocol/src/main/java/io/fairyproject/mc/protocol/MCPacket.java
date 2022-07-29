package io.fairyproject.mc.protocol;

import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;

public interface MCPacket {

    void read(FriendlyByteBuf byteBuf);

    void write(FriendlyByteBuf byteBuf);

}
