package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketSetCreativeSlot;
import org.bukkit.inventory.ItemStack;

public class CPacketEventsSetCreativeSlot extends PacketEventWrapper<WrapperPlayClientCreativeInventoryAction> implements CPacketSetCreativeSlot {
    public CPacketEventsSetCreativeSlot(WrapperPlayClientCreativeInventoryAction wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public int getSlot() {
        return wrapper.getSlot();
    }

    @Override
    public ItemStack getItemStack() {
        // TODO: ItemWrapper to ItemStack conversion
        throw new IllegalStateException("Todo!");
    }
}