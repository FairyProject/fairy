package io.fairyproject.bukkit.protocol.provider;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventsProvider;
import io.fairyproject.mc.protocol.packet.BufferListener;
import io.fairyproject.mc.protocol.packet.PacketListener;
import io.fairyproject.mc.protocol.packet.PacketProvider;

public abstract class AbstractPacketProviderFactory {
    protected PacketListener packetListener;
    protected BufferListener lowLevelPacketListener;

    public AbstractPacketProviderFactory setPacketListener(PacketListener packetListener) {
        this.packetListener = packetListener;
        return this;
    }

    public AbstractPacketProviderFactory setLowLevelPacketListener(BufferListener lowLevelPacketListener) {
        this.lowLevelPacketListener = lowLevelPacketListener;
        return this;
    }

    protected void verify() {
        assert packetListener != null : "PacketListener cannot be null!";
        assert lowLevelPacketListener != null : "BufferListener cannot be null!";
    }

    public PacketProvider build() {
        this.verify();

        return new PacketEventsProvider(packetListener, lowLevelPacketListener);
    }
}
