package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketSpectate;

import java.util.UUID;

public class CPacketEventsSpectate extends PacketEventWrapper<WrappedPacketInSpectate> implements CPacketSpectate {
    public CPacketEventsSpectate(WrappedPacketInSpectate wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public UUID getSpectated() {
        return wrapper.getUUID();
    }
}
