package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketPositionRotation;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientPositionLook;

public class CPacketArtemisPositionRotation extends ArtemisPacketWrapper<GPacketPlayClientPositionLook> implements CPacketPositionRotation {
    public CPacketArtemisPositionRotation(GPacketPlayClientPositionLook wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public double getX() {
        return wrapper.getX();
    }

    @Override
    public double getY() {
        return wrapper.getY();
    }

    @Override
    public double getZ() {
        return wrapper.getZ();
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
