package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketWindowClick;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientWindowClick;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CPacketArtemisWindowClick extends ArtemisPacketWrapper<GPacketPlayClientWindowClick> implements CPacketWindowClick {
    public CPacketArtemisWindowClick(GPacketPlayClientWindowClick wrapper, MCPlayer channel) {
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
        return Optional.of((int) wrapper.getActionNumber());
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
