package io.fairyproject.bukkit.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface BukkitByteBufProcessor {

    void process(ByteBuf byteBuf);

    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;

}
