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

import io.fairyproject.bukkit.reflection.MinecraftReflection;
import lombok.Getter;
import lombok.Setter;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.SendableWrapper;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.wrapper.ChatComponentWrapper;
import io.fairyproject.bukkit.reflection.wrapper.PacketWrapper;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@AutowiredWrappedPacket(value = PacketType.Server.PLAYER_LIST_HEADER_FOOTER, direction = PacketDirection.WRITE)
@Getter
@Setter
public class WrappedPacketOutPlayerListHeaderAndFooter extends WrappedPacket implements SendableWrapper {

    private ChatComponentWrapper header;
    private ChatComponentWrapper footer;

    public WrappedPacketOutPlayerListHeaderAndFooter(Object packet) {
        super(packet);
    }

    public WrappedPacketOutPlayerListHeaderAndFooter(ChatComponentWrapper header, ChatComponentWrapper footer) {
        this.header = header;
        this.footer = footer;
    }

    @Override
    protected void setup() {
        this.header = readChatComponent(0);
        this.footer = readChatComponent(1);
    }

    @Override
    public Object asNMSPacket() {
        try {

            Object packet = PacketTypeClasses.Server.PLAYER_LIST_HEADER_FOOTER.newInstance();
            PacketWrapper objectWrapper = new PacketWrapper(packet);

            objectWrapper.setFieldByIndex(MinecraftReflection.getIChatBaseComponentClass(), 0, MinecraftReflection.getChatComponentConverter().getGeneric(this.header));
            objectWrapper.setFieldByIndex(MinecraftReflection.getIChatBaseComponentClass(), 1, MinecraftReflection.getChatComponentConverter().getGeneric(this.footer));

            return packet;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
