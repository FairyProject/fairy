package io.fairyproject.bukkit.protocol.packet.packetevents.v1.injector;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.netty.PacketEventsBuffer;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.netty.PacketEventsChannel;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.netty.buffer.FairyByteBuf;
import io.fairyproject.mc.protocol.packet.BufferListener;
import io.fairyproject.mc.protocol.packet.PacketInjector;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PacketEventsInjector implements PacketInjector {
    @Override
    public void inject(MCPlayer data, Channel channel, BufferListener packetListener) {
        final boolean instance = channel instanceof PacketEventsChannel;

        if (!instance) {
            throw new IllegalStateException("Channel is of invalid type! (Type not PacketEvents channel)");
        }

        final PacketEventsChannel packetEventsChannel = (PacketEventsChannel) channel;

        packetEventsChannel.getChannel().pipeline().addBefore("decoder", "fairy-handler",
                new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof ByteBuf) {
                            final FairyByteBuf byteBuf = new PacketEventsBuffer((ByteBuf) msg);
                            final boolean cancel = packetListener.handle(data, byteBuf);

                            if (cancel)
                                return;
                        }

                        super.channelRead(ctx, msg);
                    }
                });
    }
}
