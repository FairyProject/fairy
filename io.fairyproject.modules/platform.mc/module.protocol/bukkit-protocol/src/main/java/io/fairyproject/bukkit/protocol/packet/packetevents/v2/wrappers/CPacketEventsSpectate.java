package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSpectate;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketSpectate;

import java.util.UUID;

public class CPacketEventsSpectate extends PacketEventWrapper<WrapperPlayClientSpectate> implements CPacketSpectate {
    public CPacketEventsSpectate(WrapperPlayClientSpectate wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public UUID getSpectated() {
        return wrapper.getTargetUUID();
    }
}
