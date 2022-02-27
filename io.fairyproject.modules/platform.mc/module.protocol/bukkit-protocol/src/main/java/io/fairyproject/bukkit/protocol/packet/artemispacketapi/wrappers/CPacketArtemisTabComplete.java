package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketTabComplete;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientTabComplete;

public class CPacketArtemisTabComplete extends ArtemisPacketWrapper<GPacketPlayClientTabComplete> implements CPacketTabComplete {
    public CPacketArtemisTabComplete(GPacketPlayClientTabComplete wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getText() {
        return wrapper.getValue();
    }
}
