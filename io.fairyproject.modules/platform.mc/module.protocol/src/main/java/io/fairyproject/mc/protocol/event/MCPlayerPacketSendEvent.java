package io.fairyproject.mc.protocol.event;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import io.fairyproject.mc.MCPlayer;

public class MCPlayerPacketSendEvent extends MCPlayerProtocolPacketEvent {
    public MCPlayerPacketSendEvent(MCPlayer player, PacketSendEvent event) {
        super(player, event);
    }

    @Override
    public PacketSendEvent event() {
        return (PacketSendEvent) super.event();
    }
}
