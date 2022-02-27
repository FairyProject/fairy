package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketWindowClick;
import io.github.retrooper.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CPacketEventsWindowClick extends PacketEventWrapper<WrappedPacketInWindowClick> implements CPacketWindowClick {
    public CPacketEventsWindowClick(WrappedPacketInWindowClick wrapper, MCPlayer channel) {
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
    public Optional<Integer> getActionNumber() {
        return Optional.of(wrapper.getActionNumber());
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
