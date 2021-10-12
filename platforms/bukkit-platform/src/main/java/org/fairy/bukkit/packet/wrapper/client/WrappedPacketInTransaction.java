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

package org.fairy.bukkit.packet.wrapper.client;

import lombok.Getter;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@AutowiredWrappedPacket(value = PacketType.Client.TRANSACTION, direction = PacketDirection.READ)
@Getter
public final class WrappedPacketInTransaction extends WrappedPacket {

    private int windowId;
    private short actionNumber;
    private boolean accepted;

    public WrappedPacketInTransaction(final Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {
        this.windowId = readInt(0);
        this.actionNumber = readShort(0);
        this.accepted = readBoolean(0);
    }

}
