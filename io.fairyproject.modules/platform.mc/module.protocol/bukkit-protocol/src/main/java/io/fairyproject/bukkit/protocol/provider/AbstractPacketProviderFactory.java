package io.fairyproject.bukkit.protocol.provider;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventsV1Provider;
import io.fairyproject.mc.protocol.InternalBufferListener;
import io.fairyproject.mc.protocol.InternalPacketListener;
import io.fairyproject.mc.protocol.PacketProvider;

public abstract class AbstractPacketProviderFactory {
    protected InternalPacketListener packetListener;
    protected InternalBufferListener lowLevelPacketListener;

    public AbstractPacketProviderFactory setPacketListener(InternalPacketListener packetListener) {
        this.packetListener = packetListener;
        return this;
    }

    public AbstractPacketProviderFactory setLowLevelPacketListener(InternalBufferListener lowLevelPacketListener) {
        this.lowLevelPacketListener = lowLevelPacketListener;
        return this;
    }

    protected void verify() {
        assert packetListener != null : "PacketListener cannot be null!";
        assert lowLevelPacketListener != null : "BufferListener cannot be null!";
    }

    public PacketProvider build() {
        this.verify();

        return new PacketEventsV1Provider(packetListener, lowLevelPacketListener);
    }
}
