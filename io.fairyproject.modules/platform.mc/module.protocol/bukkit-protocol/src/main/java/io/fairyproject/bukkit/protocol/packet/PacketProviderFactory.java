package io.fairyproject.bukkit.protocol.packet;

import io.fairyproject.mc.protocol.factory.Factory;
import io.fairyproject.mc.protocol.packet.LowLevelPacketListener;
import io.fairyproject.mc.protocol.packet.PacketListener;
import io.fairyproject.mc.protocol.packet.PacketProvider;
import io.fairyproject.mc.protocol.spigot.packet.artemispacketapi.PacketArtemisProvider;

public final class PacketProviderFactory implements Factory<PacketProvider> {
    private PacketListener packetListener;
    private LowLevelPacketListener lowLevelPacketListener;

    public PacketProviderFactory setPacketListener(PacketListener packetListener) {
        this.packetListener = packetListener;
        return this;
    }

    public PacketProviderFactory setLowLevelPacketListener(LowLevelPacketListener lowLevelPacketListener) {
        this.lowLevelPacketListener = lowLevelPacketListener;
        return this;
    }

    @Override
    public PacketProvider build() {
        assert packetListener != null : "PacketListener cannot be null!";
        assert lowLevelPacketListener != null : "LowLevelPacketListener cannot be null!";

        return new PacketArtemisProvider(packetListener, lowLevelPacketListener);
    }
}
