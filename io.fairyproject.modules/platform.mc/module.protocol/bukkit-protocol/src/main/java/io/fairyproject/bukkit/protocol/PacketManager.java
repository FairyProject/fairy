package io.fairyproject.bukkit.protocol;

import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventsV2Provider;
import io.fairyproject.bukkit.protocol.provider.AbstractPacketProviderFactory;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.library.Library;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;
import io.fairyproject.mc.protocol.InternalBufferListener;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.InternalPacketListener;
import io.fairyproject.mc.protocol.PacketProvider;
import lombok.Getter;

@Service
public class PacketManager {
    @Getter
    private PacketProvider provider;

    @PreInitialize
    public void load() {
        Library.builder()
                .groupId("com.github.retrooper.packetevents")
                .artifactId("spigot")
                .version()
        provider = new AbstractPacketProviderFactory() {
                @Override
                public PacketProvider build() {
                    this.verify();

                    return new PacketEventsV2Provider(packetListener, lowLevelPacketListener);
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
