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

package org.fairy.bukkit.packet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.fairy.*;
import org.fairy.bean.*;
import org.fairy.bukkit.util.TaskUtil;
import org.fairy.bukkit.Imanity;
import org.fairy.bukkit.packet.netty.INettyInjection;
import org.fairy.bukkit.packet.netty.NettyInjection1_8;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.type.PacketTypeClasses;
import org.fairy.bukkit.packet.wrapper.PacketContainer;
import org.fairy.bukkit.packet.wrapper.SendableWrapper;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.reflect.ReflectLookup;

import java.lang.reflect.Method;
import java.util.Collections;

@Service(name = "packet")
public class PacketService {

    public static final String CHANNEL_HANDLER = Fairy.METADATA_PREFIX + "ChannelHandler";

    @Autowired
    private static PacketService INSTANCE;

    @Autowired
    private BeanContext beanContext;

    public static void send(Player player, SendableWrapper sendableWrapper) {
        PacketService.INSTANCE.sendPacket(player, sendableWrapper);
    }

    private final Multimap<Class<?>, PacketListener> registeredPacketListeners = HashMultimap.create();

    @Getter
    private INettyInjection nettyInjection;

    @PostInitialize
    public void init() {

        try {

            Class.forName("io.netty.channel.Channel");
            nettyInjection = new NettyInjection1_8();

        } catch (ClassNotFoundException ex) {

//            nettyInjection = new NettyInjection1_7();

        }

        PacketTypeClasses.load();
        WrappedPacket.init();

        try {
            nettyInjection.registerChannels();
        } catch (Throwable throwable) {
            Imanity.LOGGER.info("Late Bind was enabled, late inject channels.");
            TaskUtil.runScheduled(() -> {
                try {
                    nettyInjection.registerChannels();
                } catch (Throwable throwable1) {
                    throw new RuntimeException(throwable1);
                }
            }, 0L);
        }

        Beans.inject(nettyInjection);
        Imanity.getPlayers().forEach(this::inject);

        try {

            this.loadWrappers();

        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong while loading wrapped packets", throwable);
        }
    }

    private void loadWrappers() throws Throwable {
        ImmutableMap.Builder<Byte, Class<? extends WrappedPacket>> readBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Byte, Class<? extends WrappedPacket>> writeBuilder = ImmutableMap.builder();

        ReflectLookup reflectLookup = new ReflectLookup(
                Collections.singleton(PacketService.class.getClassLoader()),
                Collections.singleton("org/fairy")
        );

        for (java.lang.Class<?> originalType : reflectLookup.findAnnotatedClasses(AutowiredWrappedPacket.class)) {

            if (!WrappedPacket.class.isAssignableFrom(originalType)) {
                throw new IllegalArgumentException("The type " + originalType.getName() + " does not extend WrappedPacket!");
            }

            Class<? extends WrappedPacket> type = (Class<? extends WrappedPacket>) originalType;

            try {
                AutowiredWrappedPacket annotation = type.getAnnotation(AutowiredWrappedPacket.class);

                Method method = type.getDeclaredMethod("init");
                method.invoke(null);

                switch (annotation.direction()) {
                    case READ:
                        readBuilder.put(annotation.value(), type);
                        break;
                    case WRITE:
                        writeBuilder.put(annotation.value(), type);
                        break;
                }
            } catch (NoSuchMethodException ex) {
                // Ignores
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        PacketDirection.READ.register(readBuilder.build());
        PacketDirection.WRITE.register(writeBuilder.build());
    }

    @PostDestroy
    public void stop() {
        this.nettyInjection.unregisterChannels();
    }

    public void registerPacketListener(PacketListener packetListener) {
        for (Class<?> type : packetListener.type()) {
            if (type == null) {
                throw new UnsupportedOperationException("There is one packet doesn't exists in current version!");
            }

            this.registeredPacketListeners.put(type, packetListener);
        }
    }

    public void inject(Player player) {
        this.nettyInjection.inject(player);
    }

    public void eject(Player player) {
        this.nettyInjection.eject(player);
    }

    public Object read(Player player, Object packet) {
        Class<?> type = packet.getClass();

        if (!this.registeredPacketListeners.containsKey(type)) {
            return packet;
        }

        WrappedPacket wrappedPacket = PacketDirection.READ.getWrappedFromNMS(player, PacketType.Client.getIdByType(type), packet);

        PacketDto packetDto = new PacketDto(wrappedPacket);

        boolean cancelled = false;
        for (PacketListener packetListener : this.registeredPacketListeners.get(type)) {
            if (!packetListener.read(player, packetDto)) {
                cancelled = true;
            }
        }

        return cancelled ? null : packet;
    }

    public Object write(Player player, Object packet) {
        Class<?> type = packet.getClass();

        if (!this.registeredPacketListeners.containsKey(type)) {
            return packet;
        }

        WrappedPacket wrappedPacket = PacketDirection.WRITE.getWrappedFromNMS(player, PacketType.Server.getIdByType(type), packet);

        PacketDto packetDto = new PacketDto(wrappedPacket);

        boolean cancelled = false;
        for (PacketListener packetListener : this.registeredPacketListeners.get(type)) {
            if (!packetListener.write(player, packetDto)) {
                cancelled = true;
            }
        }

        return cancelled ? null : packetDto.isRefresh() ? ((SendableWrapper) wrappedPacket).asNMSPacket() : packet;
    }

    public void sendPacket(Player player, SendableWrapper packet) {
        PacketContainer packetContainer = packet.asPacketContainer();
        MinecraftReflection.sendPacket(player, packetContainer.getMainPacket());

        for (Object extra : packetContainer.getExtraPackets()) {
            MinecraftReflection.sendPacket(player, extra);
        }
    }

}
