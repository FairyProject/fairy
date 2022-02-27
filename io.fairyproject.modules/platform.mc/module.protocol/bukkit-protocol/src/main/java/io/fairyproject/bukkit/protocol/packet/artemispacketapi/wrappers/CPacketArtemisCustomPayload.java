package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.netty.buffer.ByteArrayByteBuf;
import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;
import io.fairyproject.mc.protocol.packet.client.CPacketCustomPayload;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientCustomPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class CPacketArtemisCustomPayload extends ArtemisPacketWrapper<GPacketPlayClientCustomPayload> implements CPacketCustomPayload {
    public CPacketArtemisCustomPayload(GPacketPlayClientCustomPayload wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getHeader() {
        return wrapper.getHeader();
    }

    @Override
    public FairyByteBuf getData() {
        final byte[] data = Unpooled.buffer().readBytes((ByteBuf) wrapper.getMessage().getByteBuf().getParent()).array();

        return new ByteArrayByteBuf(data);
    }
}
