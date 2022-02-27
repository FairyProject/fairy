package io.fairyproject.bukkit.protocol.provider;

import io.fairyproject.mc.protocol.packet.BufferListener;
import io.fairyproject.mc.protocol.packet.PacketListener;
import io.fairyproject.mc.protocol.packet.PacketProvider;

interface PacketProviderFactory {
    PacketProviderFactory setPacketListener(PacketListener packetListener);

    PacketProviderFactory setLowLevelPacketListener(BufferListener bufferListener);

    void verify();

    PacketProvider build();
}
