package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketWindowClick;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CPacketEventsWindowClick extends PacketEventWrapper<WrapperPlayClientClickWindow> implements CPacketWindowClick {
    public CPacketEventsWindowClick(WrapperPlayClientClickWindow wrapper, Channel channel) {
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
    public Optional<Integer> getActionNumber() {
        return wrapper.getStateId();
    }

    @Override
    public int getMode() {
        return wrapper.getWindowClickType().ordinal();
    }

    @Override
    public ItemStack getItemStack() {
        // TODO: ItemWrapper to ItemStack conversion
        throw new IllegalStateException("Todo!");
    }
}
