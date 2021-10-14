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

package io.fairyproject.bukkit.packet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.resolver.ConstructorResolver;
import org.bukkit.entity.Player;
import io.fairyproject.bukkit.reflection.wrapper.ConstructorWrapper;

import java.util.Map;

public enum PacketDirection {

    READ,
    WRITE;

    private final Multimap<Byte, PacketListener> registeredPacketListeners = HashMultimap.create();
    private Map<Byte, Class<? extends WrappedPacket>> typeToWrappedPacket;

    public void register(Map<Byte, Class<? extends WrappedPacket>> typeToWrappedPacket) {
        if (this.typeToWrappedPacket != null) {
            throw new IllegalStateException("The Wrapped Packet are already registered!");
        }
        this.typeToWrappedPacket = typeToWrappedPacket;
    }

    public byte getPacketType(Object packet) {

        switch (this) {

            case READ:
                return PacketType.Client.getIdByType(packet.getClass());

            case WRITE:
                return PacketType.Server.getIdByType(packet.getClass());

        }

        return -1;

    }

    public boolean isPacketListening(byte id) {
        return this.registeredPacketListeners.containsKey(id);
    }

    public WrappedPacket getWrappedFromNMS(Player player, byte id, Object packet) {

        Class<? extends WrappedPacket> wrappedPacketClass = this.typeToWrappedPacket.getOrDefault(id, null);

        if (wrappedPacketClass == null) {
            return new WrappedPacket(player, packet);
        }


        ConstructorResolver constructorResolver = new ConstructorResolver(wrappedPacketClass);
        ConstructorWrapper<? extends WrappedPacket> constructor = constructorResolver.resolveWrapper(new Class[] { Player.class, Object.class });

        if (constructor.exists()) {
            return constructor.newInstance(player, packet);
        }

        constructor = constructorResolver.resolveWrapper(new Class[] { Object.class });

        if (constructor.exists()) {
            return constructor.newInstance(packet);
        }

        throw new IllegalArgumentException();
    }

    public WrappedPacket getWrappedFromNMS(Player player, byte id) {

        Class<? extends WrappedPacket> wrappedPacketClass = this.typeToWrappedPacket.getOrDefault(id, null);

        if (wrappedPacketClass == null) {
            return new WrappedPacket(player);
        }

        return (WrappedPacket)  new ConstructorResolver(wrappedPacketClass)
                .resolveMatches(
                        new Class[] { Player.class },
                        new Class[0])
                .resolve(
                        new Object[] { player },
                        new Object[0]
                );

    }

}
