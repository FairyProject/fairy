/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.tests.mc.protocol;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.PacketSender;
import io.fairyproject.mc.protocol.packet.impl.PacketPool;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketSenderMock implements PacketSender {

    private final Map<UUID, PacketPool> pools = new ConcurrentHashMap<>();

    @Override
    public void sendPacket(MCPlayer mcPlayer, PacketWrapper<?> packetWrapper) {
        this.getPool(mcPlayer).add(packetWrapper);
    }

    public PacketPool getPool(MCPlayer mcPlayer) {
        return this.pools.computeIfAbsent(mcPlayer.getUUID(), i -> new PacketPool(mcPlayer.getUUID()));
    }

}
