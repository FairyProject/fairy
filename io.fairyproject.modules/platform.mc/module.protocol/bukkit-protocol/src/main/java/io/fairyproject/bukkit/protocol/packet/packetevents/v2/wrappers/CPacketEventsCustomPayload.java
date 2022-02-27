package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventV2Wrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.netty.buffer.ByteArrayByteBuf;
import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;
import io.fairyproject.mc.protocol.packet.client.CPacketCustomPayload;

public class CPacketEventsCustomPayload extends PacketEventV2Wrapper<WrapperPlayClientPluginMessage> implements CPacketCustomPayload {
    public CPacketEventsCustomPayload(WrapperPlayClientPluginMessage wrapper, MCPlayer channel) {
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
