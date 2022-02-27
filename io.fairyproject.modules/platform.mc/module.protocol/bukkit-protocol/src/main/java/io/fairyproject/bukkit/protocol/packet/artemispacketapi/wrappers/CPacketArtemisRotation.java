package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketRotation;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientLook;

public class CPacketArtemisRotation extends ArtemisPacketWrapper<GPacketPlayClientLook> implements CPacketRotation {
    public CPacketArtemisRotation(GPacketPlayClientLook wrapper, MCPlayer channel) {
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
