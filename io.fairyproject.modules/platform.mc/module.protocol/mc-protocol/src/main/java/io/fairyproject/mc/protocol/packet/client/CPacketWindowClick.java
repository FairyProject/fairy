package io.fairyproject.mc.protocol.packet.client;

import java.util.Optional;

public interface CPacketWindowClick extends CPacket {
    int getWindowId();

    int getWindowSlot();

    int getWindowButton();

    Optional<Integer> getActionNumber();

    int getMode();

    <T> T getItemStack();

    @Override
    default String getFancyName() {
        return "WindowClick";
    }
}
