package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketHeldItemSlot;
import io.github.retrooper.packetevents.packetwrappers.play.in.helditemslot.WrappedPacketInHeldItemSlot;

public class CPacketEventsHeldItemSlot extends PacketEventWrapper<WrappedPacketInHeldItemSlot> implements CPacketHeldItemSlot {
    public CPacketEventsHeldItemSlot(WrappedPacketInHeldItemSlot wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public int getSlot() {
        return wrapper.getCurrentSelectedSlot();
    }
}
