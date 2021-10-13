package org.fairy.mc.protocol;

import org.fairy.mc.protocol.netty.FriendlyByteBuf;

public interface MCPacket {

    void read(FriendlyByteBuf byteBuf);

    void write(FriendlyByteBuf byteBuf);

}
