package io.fairyproject.mc.protocol.packet.client;

public interface CPacketFlying extends CPacket {
    boolean isGround();

    @Override
    default String getFancyName() {
        return "CPacketFlying";
    }
}
