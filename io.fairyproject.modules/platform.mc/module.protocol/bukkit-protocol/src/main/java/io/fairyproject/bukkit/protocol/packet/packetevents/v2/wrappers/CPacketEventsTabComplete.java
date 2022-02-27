package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketTabComplete;

public class CPacketEventsTabComplete extends PacketEventWrapper<WrapperPlayClientTabComplete> implements CPacketTabComplete {
    public CPacketEventsTabComplete(WrapperPlayClientTabComplete wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getText() {
        return wrapper.getText();
    }
}
