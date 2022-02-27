package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketTabComplete;
import io.github.retrooper.packetevents.packetwrappers.play.in.tabcomplete.WrappedPacketInTabComplete;

public class CPacketEventsTabComplete extends PacketEventWrapper<WrappedPacketInTabComplete> implements CPacketTabComplete {
    public CPacketEventsTabComplete(WrappedPacketInTabComplete wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public String getText() {
        return wrapper.getText();
    }
}
