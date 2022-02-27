package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketWindowClick;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import io.github.retrooper.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CPacketEventsWindowClick extends PacketEventWrapper<WrappedPacketInWindowClick> implements CPacketWindowClick {
    public CPacketEventsWindowClick(WrappedPacketInWindowClick wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public int getWindowId() {
        return wrapper.getWindowId();
    }

    @Override
    public int getWindowSlot() {
        return wrapper.getWindowSlot();
    }

    @Override
    public int getWindowButton() {
        return wrapper.getWindowButton();
    }

    @Override
    public Optional<Short> getActionNumber() {
        return wrapper.getActionNumber();
    }

    @Override
    public int getMode() {
        return wrapper.getMode();
    }

    @Override
    public ItemStack getItemStack() {
        return wrapper.getClickedItemStack();
    }
}
