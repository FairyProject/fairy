package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketTabComplete;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientTabComplete;

public class CPacketArtemisTabComplete extends PacketEventWrapper<GPacketPlayClientTabComplete> implements CPacketTabComplete {
    public CPacketArtemisTabComplete(GPacketPlayClientTabComplete wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getText() {
        return wrapper.getValue();
    }
}
