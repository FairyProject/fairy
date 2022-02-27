package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketSetCreativeSlot;
import io.github.retrooper.packetevents.packetwrappers.play.in.setcreativeslot.WrappedPacketInSetCreativeSlot;
import org.bukkit.inventory.ItemStack;

public class CPacketEventsSetCreativeSlot extends PacketEventWrapper<WrappedPacketInSetCreativeSlot> implements CPacketSetCreativeSlot {
    public CPacketEventsSetCreativeSlot(WrappedPacketInSetCreativeSlot wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public int getSlot() {
        return wrapper.getSlot();
    }

    @Override
    public ItemStack getItemStack() {
        return wrapper.getClickedItem();
    }
}
