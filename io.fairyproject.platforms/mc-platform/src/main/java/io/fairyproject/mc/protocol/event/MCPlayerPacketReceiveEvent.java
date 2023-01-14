package io.fairyproject.mc.protocol.event;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import io.fairyproject.mc.MCPlayer;

public class MCPlayerPacketReceiveEvent extends MCPlayerProtocolPacketEvent {
    public MCPlayerPacketReceiveEvent(MCPlayer player, PacketReceiveEvent event) {
        super(player, event);
    }

    @Override
    public PacketReceiveEvent getEvent() {
        return (PacketReceiveEvent) super.getEvent();
    }
}
