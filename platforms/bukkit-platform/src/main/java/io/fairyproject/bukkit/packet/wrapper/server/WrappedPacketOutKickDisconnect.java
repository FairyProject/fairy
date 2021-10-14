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

package io.fairyproject.bukkit.packet.wrapper.server;

import lombok.Getter;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.SendableWrapper;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@AutowiredWrappedPacket(value = PacketType.Server.KICK_DISCONNECT, direction = PacketDirection.WRITE)
@Getter
public final class WrappedPacketOutKickDisconnect extends WrappedPacket implements SendableWrapper {
    private static Class<?> packetClass, iChatBaseComponentClass;
    private static Constructor<?> kickDisconnectConstructor;
    private String kickMessage;

    public WrappedPacketOutKickDisconnect(final Object packet) {
        super(packet);
    }

    public WrappedPacketOutKickDisconnect(final String kickMessage) {
        super();
        this.kickMessage = kickMessage;
    }

    public static void init() {
        packetClass = PacketTypeClasses.Server.KICK_DISCONNECT;
        try {
            iChatBaseComponentClass = NMS_CLASS_RESOLVER.resolve("IChatBaseComponent");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            kickDisconnectConstructor = packetClass.getConstructor(iChatBaseComponentClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setup() {
        Object ichatbaseComponentoObject = readObject(0, iChatBaseComponentClass);
        this.kickMessage = WrappedPacketOutChat.toStringFromIChatBaseComponent(ichatbaseComponentoObject);
    }

    @Override
    public Object asNMSPacket() {
        try {
            return kickDisconnectConstructor.newInstance(WrappedPacketOutChat.toIChatBaseComponent(WrappedPacketOutChat.fromStringToJSON(kickMessage)));
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
