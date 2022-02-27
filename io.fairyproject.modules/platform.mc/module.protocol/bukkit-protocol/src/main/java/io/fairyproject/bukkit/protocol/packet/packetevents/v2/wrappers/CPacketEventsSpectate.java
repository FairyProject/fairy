package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSpectate;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventV2Wrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketSpectate;

import java.util.UUID;

public class CPacketEventsSpectate extends PacketEventV2Wrapper<WrapperPlayClientSpectate> implements CPacketSpectate {
    public CPacketEventsSpectate(WrapperPlayClientSpectate wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public UUID getSpectated() {
        return wrapper.getTargetUUID();
    }
}
