package io.fairyproject.mc.protocol.packet.client;

public interface CPacketHeldItemSlot extends CPacket{
    int getSlot();

    @Override
    default String getFancyName() {
        return "ItemSlot";
    }
}
