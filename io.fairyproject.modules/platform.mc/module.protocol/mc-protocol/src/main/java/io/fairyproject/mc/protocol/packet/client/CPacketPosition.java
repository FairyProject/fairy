package io.fairyproject.mc.protocol.packet.client;

public interface CPacketPosition extends CPacketFlying {

    double getX();

    double getY();

    double getZ();

    @Override
    default String getFancyName() {
        return "Position";
    }
}
