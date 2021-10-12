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

import com.google.common.collect.ImmutableBiMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scoreboard.DisplaySlot;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.type.PacketTypeClasses;
import org.fairy.bukkit.packet.wrapper.SendableWrapper;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.reflection.wrapper.PacketWrapper;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@AutowiredWrappedPacket(value = PacketType.Server.SCOREBOARD_DISPLAY_OBJECTIVE, direction = PacketDirection.WRITE)
@Getter
@Setter
public class WrappedPacketOutScoreboardDisplayObjective extends WrappedPacket implements SendableWrapper {

    private static ImmutableBiMap<DisplaySlot, Integer> DISPLAY_SLOT_TO_ID;

    public static void init() {

        DISPLAY_SLOT_TO_ID = ImmutableBiMap.<DisplaySlot, Integer>builder()
                .put(DisplaySlot.PLAYER_LIST, 0)
                .put(DisplaySlot.SIDEBAR, 1)
                .put(DisplaySlot.BELOW_NAME, 2)
                .build();

    }

    private DisplaySlot displaySlot;
    private String objective;

    public WrappedPacketOutScoreboardDisplayObjective(Object packet) {
        super(packet);
    }

    public WrappedPacketOutScoreboardDisplayObjective(DisplaySlot displaySlot, String objective) {
        this.displaySlot = displaySlot;
        this.objective = objective;
    }

    @Override
    protected void setup() {
        this.displaySlot = DISPLAY_SLOT_TO_ID.inverse().get(readInt(0));
        this.objective = readString(0);
    }

    @Override
    public Object asNMSPacket() {
        return new PacketWrapper(PacketTypeClasses.Server.SCOREBOARD_DISPLAY_OBJECTIVE)
                .setFieldByIndex(int.class, 0, DISPLAY_SLOT_TO_ID.get(this.displaySlot))
                .setFieldByIndex(String.class, 0, this.objective)
                .getPacket();
    }
}
