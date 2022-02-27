package io.fairyproject.bukkit.protocol.packet.packetevents.v2.injector;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.netty.PacketEventsBuffer;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.InternalBufferListener;
import io.fairyproject.mc.protocol.PacketInjector;
import io.fairyproject.mc.protocol.netty.Channel;
import io.netty.buffer.ByteBuf;

public class PacketEventsInjector implements PacketInjector {
    @Override
    public void inject(MCPlayer data, Channel channel, InternalBufferListener packetListener) {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                packetListener.handle(data, new PacketEventsBuffer((ByteBuf) event.getByteBuf()));
            }

            @Override
            public void onPacketSend(PacketSendEvent event) {
                packetListener.handle(data, new PacketEventsBuffer((ByteBuf) event.getByteBuf()));
            }
        });
    }
}
