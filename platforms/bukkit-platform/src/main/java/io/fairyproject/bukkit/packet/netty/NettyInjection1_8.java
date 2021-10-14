/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.bukkit.packet.netty;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.client.login.WrappedPacketInLoginStart;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.Setter;
import org.bukkit.entity.Player;
import io.fairyproject.bukkit.packet.PacketService;
import io.fairyproject.bukkit.Imanity;
import io.fairyproject.bukkit.packet.collection.BootstrapList;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bean.Autowired;

import java.util.*;

public class NettyInjection1_8 implements INettyInjection {

    @Autowired
    private PacketService packetService;

    private boolean closed;

    private final List<Channel> serverChannels = Lists.newArrayList();
    private final Set<Channel> uninjectedChannels = Collections.newSetFromMap(new MapMaker().weakKeys().<Channel, Boolean>makeMap());

    private final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();

    private ChannelInboundHandlerAdapter serverChannelHandler;
    private ChannelInitializer<Channel> beginInitProtocol;
    private ChannelInitializer<Channel> endInitProtocol;

    @Override
    public void inject(Player player) {
        Channel channel = this.getChannel(player);

        if (uninjectedChannels.contains(channel)) {
            return;
        }

        this.injectChannelInternal(channel).setPlayer(player);
    }

    private PacketHandler injectChannelInternal(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        PacketHandler packetHandler = (PacketHandler) pipeline.get(PacketService.CHANNEL_HANDLER);

        if (packetHandler == null) {
            packetHandler = new PacketHandler();
            pipeline.addBefore("packet_handler", PacketService.CHANNEL_HANDLER, packetHandler);
            uninjectedChannels.remove(channel);
        }
        
        return packetHandler;
    }

    @Override
    public void eject(Player player) {
        Channel channel = this.getChannel(player);
        this.ejectChannel(channel);
    }

    public void ejectChannel(final Channel channel) {
        // No need to guard against this if we're closing
        if (!closed) {
            this.uninjectedChannels.add(channel);
        }

        // See ChannelInjector in ProtocolLib, line 590
        channel.eventLoop().execute(() -> {
            try {
                channel.pipeline().remove(PacketService.CHANNEL_HANDLER);
            } catch (NoSuchElementException ignored) {
                // It's fine (?)
            }
        });
    }

    public Channel getChannel(Player player) {
        Channel channel = this.channelLookup.get(player.getName());

        if (channel == null) {
            channel = MinecraftReflection.getChannel(player);
        }

        return channel;
    }

    private List<?> networkManagers;

    @Override
    public void registerChannels() throws Exception {
        NMSClassResolver classResolver = new NMSClassResolver();
        Class<?> minecraftServerClass = classResolver.resolve("MinecraftServer");
        Class<?> serverConnectionClass = classResolver.resolve("ServerConnection");
        FieldResolver mcFieldResolver = new FieldResolver(minecraftServerClass);

        Object minecraftServer = mcFieldResolver.resolve(minecraftServerClass, 0).get(null);
        Object serverConnection = mcFieldResolver.resolve(serverConnectionClass, 0).get(minecraftServer);

        FieldResolver serverConnectionFieldResolver = new FieldResolver(serverConnection.getClass());
        this.networkManagers = serverConnectionFieldResolver.resolveWithGenericType(List.class, classResolver.resolve("NetworkManager")).get(serverConnection);

        endInitProtocol = new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel channel) throws Exception {
                try {
                    // This can take a while, so we need to stop the main thread from interfering
                    synchronized (networkManagers) {
                        // Stop injecting channels
                        if (!closed) {
                            channel.eventLoop().submit(() -> injectChannelInternal(channel));
                        }
                    }
                } catch (Exception e) {
                    Imanity.LOGGER.error("Cannot inject incomming channel " + channel, e);
                }
            }

        };

        // This is executed before Minecraft's channel handler
        beginInitProtocol = new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(endInitProtocol);
            }

        };

        serverChannelHandler = new ChannelInboundHandlerAdapter() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Channel channel = (Channel) msg;

                // Prepare to initialize ths channel
                channel.pipeline().addFirst(beginInitProtocol);
                ctx.fireChannelRead(msg);
            }

        };

        for (FieldWrapper<List> field : serverConnectionFieldResolver.resolveList(List.class)) {

            List list = field.get(serverConnection);
            if (list.size() == 0 || list.iterator().next() instanceof ChannelFuture) {
                field.set(serverConnection, new BootstrapList(list, serverChannelHandler));
            }

        }

    }

    @Override
    public void unregisterChannels() {
        if (!this.closed) {
            this.closed = true;

            this.unregisterChannelHandler();
        }
    }

    private void unregisterChannelHandler() {
        if (serverChannelHandler == null)
            return;

        for (Player player : Imanity.getPlayers()) {
            this.eject(player);
        }

        for (Channel serverChannel : serverChannels) {
            final ChannelPipeline pipeline = serverChannel.pipeline();

            // Remove channel handler
            serverChannel.eventLoop().execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        pipeline.remove(serverChannelHandler);
                    } catch (NoSuchElementException e) {
                        // That's fine
                    }
                }

            });
            this.serverChannels.remove(serverChannel);
        }
    }

    @Override
    public byte[] readBytes(Object byteBuffer) {
        ByteBuf byteBuf = (ByteBuf) byteBuffer;
        byte[] array = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(array);
        return array;
    }

    @Setter
    public class PacketHandler extends ChannelDuplexHandler {

        private volatile Player player;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Object packet = packetService.read(player, msg);

            if (packet == null) {
                return;
            }

            this.handleLoginStart(ctx.channel(), packet);
            super.channelRead(ctx, packet);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            Object packet = packetService.write(player, msg);
            if (packet == null) {
                return;
            }

            super.write(ctx, packet, promise);
        }

        private void handleLoginStart(Channel channel, Object msg) {
            if (PacketTypeClasses.Client.LOGIN_START.isInstance(msg)) {
                WrappedPacketInLoginStart packet = new WrappedPacketInLoginStart(msg);
                channelLookup.put(packet.getGameProfile().getName(), channel);
            }
        }

    }
}
