package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketSpectate;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientSpectate;

import java.util.UUID;

public class CPacketArtemisSpectate extends ArtemisPacketWrapper<GPacketPlayClientSpectate> implements CPacketSpectate {
    public CPacketArtemisSpectate(GPacketPlayClientSpectate wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public UUID getSpectated() {
        return wrapper.getEntityId();
    }
}
