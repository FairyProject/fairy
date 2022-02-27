package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketChat;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import io.github.retrooper.packetevents.packetwrappers.play.in.chat.WrappedPacketInChat;

public class CPacketEventsChat extends PacketEventWrapper<WrappedPacketInChat> implements CPacketChat {
    public CPacketEventsChat(WrappedPacketInChat wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getMessage() {
        return wrapper.getMessage();
    }
}
