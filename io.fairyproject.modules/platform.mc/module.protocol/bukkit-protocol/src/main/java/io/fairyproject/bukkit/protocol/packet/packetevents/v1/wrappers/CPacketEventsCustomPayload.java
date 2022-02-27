package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketCustomPayload;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import io.fairyproject.mc.protocol.wrapper.bytebuf.ArtemisByteBuf;
import io.fairyproject.mc.protocol.wrapper.bytebuf.ByteArrayByteBuf;
import io.github.retrooper.packetevents.packetwrappers.play.in.custompayload.WrappedPacketInCustomPayload;

public class CPacketEventsCustomPayload extends PacketEventWrapper<WrappedPacketInCustomPayload> implements CPacketCustomPayload {
    public CPacketEventsCustomPayload(WrappedPacketInCustomPayload wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getHeader() {
        return wrapper.getTag();
    }

    @Override
    public ArtemisByteBuf getData() {
        final byte[] data = wrapper.getData();

        return new ByteArrayByteBuf(data);
    }
}
