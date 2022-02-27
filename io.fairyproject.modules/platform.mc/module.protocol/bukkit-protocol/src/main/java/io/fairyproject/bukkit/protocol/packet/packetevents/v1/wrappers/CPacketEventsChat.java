package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketChat;
import io.github.retrooper.packetevents.packetwrappers.play.in.chat.WrappedPacketInChat;

public class CPacketEventsChat extends PacketEventWrapper<WrappedPacketInChat> implements CPacketChat {
    public CPacketEventsChat(WrappedPacketInChat wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public String getMessage() {
        return wrapper.getMessage();
    }
}
