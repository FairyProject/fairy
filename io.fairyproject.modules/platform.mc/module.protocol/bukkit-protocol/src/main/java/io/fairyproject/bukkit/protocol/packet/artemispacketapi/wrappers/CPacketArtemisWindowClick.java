package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketWindowClick;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientWindowClick;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CPacketArtemisWindowClick extends PacketEventWrapper<GPacketPlayClientWindowClick> implements CPacketWindowClick {
    public CPacketArtemisWindowClick(GPacketPlayClientWindowClick wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public int getWindowId() {
        return wrapper.getWindowId();
    }

    @Override
    public int getWindowSlot() {
        return wrapper.getSlot();
    }

    @Override
    public int getWindowButton() {
        return wrapper.getButton();
    }

    @Override
    public Optional<Short> getActionNumber() {
        return Optional.of(wrapper.getActionNumber());
    }

    @Override
    public int getMode() {
        return wrapper.getShiftedMode();
    }

    @Override
    public ItemStack getItemStack() {
        return wrapper.getClickedItem();
    }
}
