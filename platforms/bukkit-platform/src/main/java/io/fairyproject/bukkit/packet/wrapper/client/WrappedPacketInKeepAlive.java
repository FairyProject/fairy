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
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;

@AutowiredWrappedPacket(value = PacketType.Client.KEEP_ALIVE, direction = PacketDirection.READ)
@Getter
public final class WrappedPacketInKeepAlive extends WrappedPacket {
    private static boolean integerPresent;
    private long id;

    public WrappedPacketInKeepAlive(final Object packet) {
        super(packet);
    }

    public static void init() {
        integerPresent = new FieldResolver(PacketTypeClasses.Client.KEEP_ALIVE)
            .resolveSilent(int.class, 0)
            .exists();
    }

    @Override
    protected void setup() {
        if (!integerPresent) {
            this.id = readLong(0);
        } else {
            this.id = readInt(0);
        }
    }
}
