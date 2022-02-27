package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketHeldItemSlot;

public class CPacketEventsHeldItemSlot extends PacketEventWrapper<WrapperPlayClientHeldItemChange> implements CPacketHeldItemSlot {
    public CPacketEventsHeldItemSlot(WrapperPlayClientHeldItemChange wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public int getSlot() {
        return wrapper.getSlot();
    }
}
