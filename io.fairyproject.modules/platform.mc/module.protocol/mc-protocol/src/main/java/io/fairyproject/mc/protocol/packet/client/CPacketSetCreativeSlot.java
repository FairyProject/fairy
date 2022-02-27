package io.fairyproject.mc.protocol.packet.client;

public interface CPacketSetCreativeSlot extends CPacket {
    int getSlot();

    <T> T getItemStack();

    @Override
    default String getFancyName() {
        return "SetCreativeSlot";
    }
}
