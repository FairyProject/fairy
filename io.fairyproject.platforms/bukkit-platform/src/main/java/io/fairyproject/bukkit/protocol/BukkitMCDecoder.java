package io.fairyproject.bukkit.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BukkitMCDecoder extends ByteToMessageDecoder implements BukkitByteBufProcessor {

    private final ByteToMessageDecoder minecraftDecoder;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // TODO
    }
    public void decodePublic(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        this.decode(channelHandlerContext, byteBuf, list);
    }

    @Override
    public void process(ByteBuf byteBuf) {
        // TODO
    }
}
