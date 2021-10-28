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

package io.fairyproject.bukkit.packet.wrapper.client;

import lombok.Getter;
import lombok.SneakyThrows;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@AutowiredWrappedPacket(value = PacketType.Client.CUSTOM_PAYLOAD, direction = PacketDirection.READ)
@Getter
public final class WrappedPacketInCustomPayload extends WrappedPacket {
    private static Class<?> packetClass, nmsMinecraftKey, nmsPacketDataSerializer;

    private static boolean strPresentInIndex0;
    private String data;
    private Object minecraftKey, dataSerializer;
    public WrappedPacketInCustomPayload(Object packet) {
        super(packet);
    }

    public static void init() {
        packetClass = PacketTypeClasses.Client.CUSTOM_PAYLOAD;
        strPresentInIndex0 = new FieldResolver(packetClass)
            .resolveSilent(String.class, 0)
            .exists();
        try {
            nmsPacketDataSerializer = NMS_CLASS_RESOLVER.resolve("PacketDataSerializer");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            //Only on 1.13+
            nmsMinecraftKey = NMS_CLASS_RESOLVER.resolve("MinecraftKey");
        } catch (ClassNotFoundException e) {
            //Its okay, this means they are on versions 1.7.10 - 1.12.2
        }
    }

    @SneakyThrows
    @Override
    public void setup() {
        if (!strPresentInIndex0) {
            this.minecraftKey = readObject(0, nmsMinecraftKey);
            this.dataSerializer = readObject(0, nmsPacketDataSerializer);

        } else {
            this.data = readString(0);

            FieldWrapper<?> field = this.packet.getFieldByIndex(nmsPacketDataSerializer, 0);
            if (field != null) {
                this.dataSerializer = field.get(this.packet.getPacket());
            }
        }
    }

}
