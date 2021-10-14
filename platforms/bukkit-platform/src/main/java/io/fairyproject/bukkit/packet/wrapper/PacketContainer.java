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

package io.fairyproject.bukkit.packet.wrapper;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class PacketContainer {

    private final Object mainPacket;

    private final List<Object> extraPackets;

    private PacketContainer(Object mainPacket) {
        this(mainPacket, 0);
    }

    private PacketContainer(Object mainPacket, int expectedExtraPackets) {
        this.mainPacket = mainPacket;
        this.extraPackets = new ArrayList<>(expectedExtraPackets);
    }

    public PacketContainer addExtraPacket(Object packet) {
        this.extraPackets.add(packet);
        return this;
    }

    public PacketContainer addAll(Collection<Object> packets) {
        this.extraPackets.addAll(packets);
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PacketContainer empty() {
        return new PacketContainer(null);
    }

    public static PacketContainer of(Object mainPacket) {
        return new Builder()
                .mainPacket(mainPacket)
                .build();
    }

    public static class Builder {

        private Object mainPacket;
        private List<Object> extraPackets;

        public Builder mainPacket(Object mainPacket) {
            this.mainPacket = mainPacket;
            return this;
        }

        public Builder extraPackets(Object... extraPackets) {
            if (extraPackets == null || extraPackets.length == 0) {
                return this;
            }

            if (this.extraPackets == null) {
                this.extraPackets = new ArrayList<>(extraPackets.length);
            }
            this.extraPackets.add(extraPackets);
            return this;
        }

        public Builder extraPackets(Collection<?> extraPackets) {
            if (extraPackets == null || extraPackets.isEmpty()) {
                return this;
            }

            if (this.extraPackets == null) {
                this.extraPackets = new ArrayList<>(extraPackets.size());
            }
            this.extraPackets.addAll(extraPackets);
            return this;
        }

        public PacketContainer build() {
            if (this.extraPackets != null) {
                return new PacketContainer(this.mainPacket, this.extraPackets.size()).addAll(this.extraPackets);
            } else {
                return new PacketContainer(this.mainPacket);
            }
        }

    }

}
