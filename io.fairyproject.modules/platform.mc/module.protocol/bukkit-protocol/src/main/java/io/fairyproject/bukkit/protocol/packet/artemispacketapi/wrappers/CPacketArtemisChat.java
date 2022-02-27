package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketChat;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientChat;

public class CPacketArtemisChat extends ArtemisPacketWrapper<GPacketPlayClientChat> implements CPacketChat {
    public CPacketArtemisChat(GPacketPlayClientChat wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getMessage() {
        return wrapper.getMessage();
    }
}