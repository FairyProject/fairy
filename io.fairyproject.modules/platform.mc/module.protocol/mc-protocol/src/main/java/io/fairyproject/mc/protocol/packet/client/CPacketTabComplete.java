package io.fairyproject.mc.protocol.packet.client;

public interface CPacketTabComplete extends CPacket {

    String getText();

    @Override
    default String getFancyName() {
        return "TabComplete";
    }
}
