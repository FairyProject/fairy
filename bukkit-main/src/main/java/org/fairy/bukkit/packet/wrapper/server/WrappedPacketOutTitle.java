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

package org.fairy.bukkit.packet.wrapper.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.type.PacketTypeClasses;
import org.fairy.bukkit.packet.wrapper.SendableWrapper;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.reflection.wrapper.ChatComponentWrapper;
import org.fairy.bukkit.reflection.wrapper.PacketWrapper;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@AutowiredWrappedPacket(value = PacketType.Server.TITLE, direction = PacketDirection.WRITE)
@Getter
@Setter
@AllArgsConstructor
@Builder
public class WrappedPacketOutTitle extends WrappedPacket implements SendableWrapper {

    public static final int DEFAULT_FADE_IN = 20;
    public static final int DEFAULT_STAY = 200;
    public static final int DEFAULT_FADE_OUT = 20;

    private Action action;
    private ChatComponentWrapper message;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    public WrappedPacketOutTitle(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {

        this.action = MinecraftReflection.getTitleActionConverter().getSpecific(readObject(0, MinecraftReflection.getEnumTitleActionClass()));
        this.message = readChatComponent(0);

        this.fadeIn = readInt(0);
        this.stay = readInt(1);
        this.fadeOut = readInt(2);

    }

    @Override
    public Object asNMSPacket() {
        PacketWrapper packetWrapper = new PacketWrapper(PacketTypeClasses.Server.TITLE);
        packetWrapper.setFieldByIndex(MinecraftReflection.getEnumTitleActionClass(), 0, MinecraftReflection.getTitleActionConverter().getGeneric(this.action));
        if (this.message != null) {
            packetWrapper.setFieldByIndex(MinecraftReflection.getIChatBaseComponentClass(), 0, this.message.getHandle());
        }

        return packetWrapper.setFieldByIndex(int.class, 0, this.fadeIn)
                .setFieldByIndex(int.class, 1, this.stay)
                .setFieldByIndex(int.class, 2, this.fadeOut)
                .getPacket();
    }

    public static enum Action {
        TITLE,
        SUBTITLE,
        TIMES,
        CLEAR,
        RESET;
    }

}
