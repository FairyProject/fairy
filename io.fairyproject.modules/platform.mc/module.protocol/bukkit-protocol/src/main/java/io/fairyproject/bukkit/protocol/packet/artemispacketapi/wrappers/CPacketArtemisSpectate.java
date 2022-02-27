package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketSpectate;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientSpectate;

import java.util.UUID;

public class CPacketArtemisSpectate extends ArtemisPacketWrapper<GPacketPlayClientSpectate> implements CPacketSpectate {
    public CPacketArtemisSpectate(GPacketPlayClientSpectate wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public UUID getSpectated() {
        return wrapper.getEntityId();
    }
}
