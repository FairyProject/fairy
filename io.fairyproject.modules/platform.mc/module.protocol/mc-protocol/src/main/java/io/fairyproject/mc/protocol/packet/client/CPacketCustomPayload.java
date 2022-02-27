package io.fairyproject.mc.protocol.packet.client;


import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;

public interface CPacketCustomPayload extends CPacket {
    String getHeader();

    FairyByteBuf getData();

    @Override
    default String getFancyName() {
        return "CustomPayload";
    }
}
