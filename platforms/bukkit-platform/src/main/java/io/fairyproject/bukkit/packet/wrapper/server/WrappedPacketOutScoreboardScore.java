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
import org.bukkit.entity.Player;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.wrapper.SendableWrapper;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.wrapper.PacketWrapper;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@AutowiredWrappedPacket(value = PacketType.Server.SCOREBOARD_SCORE, direction = PacketDirection.WRITE)
@Getter
@Setter
public class WrappedPacketOutScoreboardScore extends WrappedPacket implements SendableWrapper {

    private static boolean ACTION_IS_ENUM;

    static {

        try {
            MinecraftReflection.getEnumScoreboardActionClass();
            ACTION_IS_ENUM = true;
        } catch (Throwable ignored) {
            ACTION_IS_ENUM = false;
        }

    }

    private String entry;
    private String objective;
    private int score;
    private ScoreboardAction action;

    public WrappedPacketOutScoreboardScore(Player player, Object packet) {
        super(player, packet);
    }

    public WrappedPacketOutScoreboardScore(Object packet) {
        super(packet);
    }

    public WrappedPacketOutScoreboardScore(String entry, String objective, int score, ScoreboardAction action) {
        super();
        this.entry = entry;
        this.objective = objective;
        this.score = score;
        this.action = action;
    }

    @Override
    protected void setup() {
        this.entry = readString(0);
        this.objective = readString(1);
        this.score = readInt(0);

        if (ACTION_IS_ENUM) {
            this.action = MinecraftReflection.getScoreboardActionConverter().getSpecific(readObject(0, MinecraftReflection.getEnumScoreboardActionClass()));
        } else {
            this.action = ScoreboardAction.getById(readInt(1));
        }
    }

    @Override
    public Object asNMSPacket() {
        PacketWrapper packet = PacketWrapper.createByPacketName("PacketPlayOutScoreboardScore");

        packet.setFieldByIndex(String.class, 0, this.entry);
        packet.setFieldByIndex(String.class, 1, this.objective);
        packet.setFieldByIndex(int.class, 0, this.score);

        if (ACTION_IS_ENUM) {
            packet.setFieldByIndex(MinecraftReflection.getEnumScoreboardActionClass(), 0, MinecraftReflection.getScoreboardActionConverter().getGeneric(this.action));
        } else {
            packet.setFieldByIndex(int.class, 1, this.action.getId());
        }

        return packet.getPacket();
    }

    @Getter
    public static enum ScoreboardAction {
        CHANGE(0),
        REMOVE(1);

        private final int id;

        ScoreboardAction(int id) {
            this.id = id;
        }

        public static ScoreboardAction getById(int id) {
            for (ScoreboardAction action : values()) {
                if (action.id == id) {
                    return action;
                }
            }
            return null;
        }
    }

}
