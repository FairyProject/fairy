package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketHeldItemSlot;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientHeldItemSlot;

public class CPacketArtemisHeldItemSlot extends PacketEventWrapper<GPacketPlayClientHeldItemSlot> implements CPacketHeldItemSlot {
    public CPacketArtemisHeldItemSlot(GPacketPlayClientHeldItemSlot wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public int getSlot() {
        return wrapper.getSlot();
    }
}
