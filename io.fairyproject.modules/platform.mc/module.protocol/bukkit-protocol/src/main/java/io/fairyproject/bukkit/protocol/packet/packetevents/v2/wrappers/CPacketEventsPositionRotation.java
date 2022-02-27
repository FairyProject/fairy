package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventV2Wrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketPositionRotation;

public class CPacketEventsPositionRotation extends PacketEventV2Wrapper<WrapperPlayClientPlayerPositionAndRotation> implements CPacketPositionRotation {
    public CPacketEventsPositionRotation(WrapperPlayClientPlayerPositionAndRotation wrapper, MCPlayer channel) {
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
    public float getYaw() {
        return wrapper.getYaw();
    }

    @Override
    public float getPitch() {
        return wrapper.getPitch();
    }

    @Override
    public boolean isGround() {
        return wrapper.isOnGround();
    }
}
