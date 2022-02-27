package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.netty.buffer.ByteArrayByteBuf;
import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;
import io.fairyproject.mc.protocol.packet.client.CPacketCustomPayload;

public class CPacketEventsCustomPayload extends PacketEventWrapper<WrapperPlayClientPluginMessage> implements CPacketCustomPayload {
    public CPacketEventsCustomPayload(WrapperPlayClientPluginMessage wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getHeader() {
        return wrapper.getChannelName();
    }

    @Override
    public FairyByteBuf getData() {
        final byte[] data = wrapper.getData();

        return new ByteArrayByteBuf(data);
    }
}
