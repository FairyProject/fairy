package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketSetCreativeSlot;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientSetCreativeSlot;
import org.bukkit.inventory.ItemStack;

public class CPacketArtemisSetCreativeSlot extends ArtemisPacketWrapper<GPacketPlayClientSetCreativeSlot> implements CPacketSetCreativeSlot {
    public CPacketArtemisSetCreativeSlot(GPacketPlayClientSetCreativeSlot wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public int getSlot() {
        return wrapper.getSlot();
    }

    @Override
    public ItemStack getItemStack() {
        return wrapper.getItem();
    }
}
