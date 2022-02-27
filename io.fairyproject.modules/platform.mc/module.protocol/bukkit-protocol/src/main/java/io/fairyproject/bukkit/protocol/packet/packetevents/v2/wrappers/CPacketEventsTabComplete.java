package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketTabComplete;

public class CPacketEventsTabComplete extends PacketEventWrapper<WrapperPlayClientTabComplete> implements CPacketTabComplete {
    public CPacketEventsTabComplete(WrapperPlayClientTabComplete wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public String getText() {
        return wrapper.getText();
    }
}
