package io.fairyproject.bukkit.protocol.packet;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventsV1Provider;
import io.fairyproject.bukkit.protocol.provider.AbstractPacketProviderFactory;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;
import io.fairyproject.mc.protocol.InternalBufferListener;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.InternalPacketListener;
import io.fairyproject.mc.protocol.PacketProvider;
import lombok.Getter;

public class PacketManager {
    @Getter
    private PacketProvider provider;

    public void load() {
        provider = new AbstractPacketProviderFactory() {
                @Override
                public PacketProvider build() {
                    this.verify();

                    return new PacketEventsV1Provider(packetListener, lowLevelPacketListener);
                }}
                .setPacketListener(new InternalPacketListener() {
                    @Override
                    public boolean onPacket(MCPlayer data, Packet packet) {
                        // TODO: Handle logic

                        return false;
                    }
                })
                .setLowLevelPacketListener(new InternalBufferListener() {
                    @Override
                    public boolean handle(MCPlayer data, FairyByteBuf byteBuf) {
                        // TODO: Handle logic

                        return false;
                    }
                })
                .build();

        provider.load();
    }

    public void init() {
        provider.init();
    }

    public void end() {
        provider.quit();
    }
}
