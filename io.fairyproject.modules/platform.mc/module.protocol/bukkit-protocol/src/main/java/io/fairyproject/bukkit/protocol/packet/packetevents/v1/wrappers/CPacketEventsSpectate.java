package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketSpectate;
import io.github.retrooper.packetevents.packetwrappers.play.in.spectate.WrappedPacketInSpectate;

import java.util.UUID;

public class CPacketEventsSpectate extends PacketEventWrapper<WrappedPacketInSpectate> implements CPacketSpectate {
    public CPacketEventsSpectate(WrappedPacketInSpectate wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public UUID getSpectated() {
        return wrapper.getUUID();
    }
}
