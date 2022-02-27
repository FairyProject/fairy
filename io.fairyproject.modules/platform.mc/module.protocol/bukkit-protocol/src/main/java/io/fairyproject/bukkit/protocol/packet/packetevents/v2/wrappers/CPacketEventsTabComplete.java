package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventV2Wrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketTabComplete;

public class CPacketEventsTabComplete extends PacketEventV2Wrapper<WrapperPlayClientTabComplete> implements CPacketTabComplete {
    public CPacketEventsTabComplete(WrapperPlayClientTabComplete wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public String getText() {
        return wrapper.getText();
    }
}
