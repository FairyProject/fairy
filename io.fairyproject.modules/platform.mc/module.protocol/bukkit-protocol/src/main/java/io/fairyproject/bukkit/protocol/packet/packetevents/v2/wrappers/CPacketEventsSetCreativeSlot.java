package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventV2Wrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketSetCreativeSlot;
import org.bukkit.inventory.ItemStack;

public class CPacketEventsSetCreativeSlot extends PacketEventV2Wrapper<WrapperPlayClientCreativeInventoryAction> implements CPacketSetCreativeSlot {
    public CPacketEventsSetCreativeSlot(WrapperPlayClientCreativeInventoryAction wrapper, MCPlayer channel) {
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
