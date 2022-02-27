package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketPosition;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientPosition;

public class CPacketArtemisPosition extends ArtemisPacketWrapper<GPacketPlayClientPosition> implements CPacketPosition {
    public CPacketArtemisPosition(GPacketPlayClientPosition wrapper, Channel channel) {
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
    public boolean isGround() {
        return wrapper.isOnGround();
    }
}
