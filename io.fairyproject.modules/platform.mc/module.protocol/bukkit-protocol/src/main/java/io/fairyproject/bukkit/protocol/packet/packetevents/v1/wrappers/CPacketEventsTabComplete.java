package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketTabComplete;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import io.github.retrooper.packetevents.packetwrappers.play.in.tabcomplete.WrappedPacketInTabComplete;

public class CPacketEventsTabComplete extends PacketEventWrapper<WrappedPacketInTabComplete> implements CPacketTabComplete {
    public CPacketEventsTabComplete(WrappedPacketInTabComplete wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getText() {
        return wrapper.getText();
    }
}
