package io.fairyproject.mc.protocol.packet.client;

import java.util.UUID;

public interface CPacketSpectate extends CPacket {

    UUID getSpectated();

    @Override
    default String getFancyName() {
        return "Spectate";
    }
}
