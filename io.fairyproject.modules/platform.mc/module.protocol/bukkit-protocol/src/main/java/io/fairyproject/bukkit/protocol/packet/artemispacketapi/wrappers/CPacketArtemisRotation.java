package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketRotation;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientLook;

public class CPacketArtemisRotation extends PacketEventWrapper<GPacketPlayClientLook> implements CPacketRotation {
    public CPacketArtemisRotation(GPacketPlayClientLook wrapper, Channel channel) {
        super(wrapper, channel);
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
