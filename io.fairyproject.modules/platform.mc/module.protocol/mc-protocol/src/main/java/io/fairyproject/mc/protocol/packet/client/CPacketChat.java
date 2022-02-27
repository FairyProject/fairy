package io.fairyproject.mc.protocol.packet.client;

public interface CPacketChat extends CPacket {
    String getMessage();

    @Override
    default String getFancyName() {
        return "Chat";
    }
}
