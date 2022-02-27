package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketFlying;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;

public class CPacketEventsFlying extends PacketEventWrapper<WrappedPacketInFlying> implements CPacketFlying {
    public CPacketEventsFlying(WrappedPacketInFlying wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public boolean isGround() {
        return wrapper.isOnGround();
    }
}
