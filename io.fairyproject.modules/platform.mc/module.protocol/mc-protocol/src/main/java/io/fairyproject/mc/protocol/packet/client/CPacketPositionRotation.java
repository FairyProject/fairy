package io.fairyproject.mc.protocol.packet.client;

public interface CPacketPositionRotation extends CPacketPosition, CPacketRotation {
    @Override
    default String getFancyName() {
        return "PositionRotation";
    }
}
