package io.fairyproject.mc.protocol.packet.client;

public interface CPacketRotation extends CPacketFlying {
    float getYaw();

    float getPitch();

    @Override
    default String getFancyName() {
        return "Rotation";
    }
}
