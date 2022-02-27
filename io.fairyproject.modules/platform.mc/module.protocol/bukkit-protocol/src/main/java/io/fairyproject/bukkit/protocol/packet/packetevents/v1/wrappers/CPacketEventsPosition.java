package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketPosition;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;

public class CPacketEventsPosition extends PacketEventWrapper<WrappedPacketInFlying> implements CPacketPosition {
    public CPacketEventsPosition(WrappedPacketInFlying wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public double getX() {
        return wrapper.getPosition().getX();
    }

    @Override
    public double getY() {
        return wrapper.getPosition().getY();
    }

    @Override
    public double getZ() {
        return wrapper.getPosition().getZ();
    }

    @Override
    public boolean isGround() {
        return wrapper.isOnGround();
    }
}
