package io.fairyproject.bukkit.protocol.packet.packetevents.v1.injector;

import io.fairyproject.mc.protocol.data.PlayerData;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.LowLevelPacketListener;
import io.fairyproject.mc.protocol.packet.PacketInjector;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.netty.PacketEventsBuffer;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.netty.PacketEventsChannel;
import io.fairyproject.mc.protocol.wrapper.bytebuf.ArtemisByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PacketEventsInjector implements PacketInjector {
    @Override
    public void inject(PlayerData data, Channel channel, LowLevelPacketListener packetListener) {
        final boolean instance = channel instanceof PacketEventsChannel;

        if (!instance) {
            throw new IllegalStateException("Channel is of invalid type! (Type not PacketEvents channel)");
        }

        final PacketEventsChannel packetEventsChannel = (PacketEventsChannel) channel;

        packetEventsChannel.getChannel().pipeline().addBefore("decoder", "artemis-crash-handler",
                new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof ByteBuf) {
                            final ArtemisByteBuf byteBuf = new PacketEventsBuffer((ByteBuf) msg);
                            final boolean cancel = packetListener.handle(data, byteBuf);

                            if (cancel)
                                return;
                        }

                        super.channelRead(ctx, msg);
                    }
                });
    }
}
