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

import com.google.common.collect.ImmutableMap;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.SendableWrapper;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.wrapper.PacketWrapper;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

import java.util.Map;

@AutowiredWrappedPacket(value = PacketType.Server.SCOREBOARD_OBJECTIVE, direction = PacketDirection.WRITE)
@Getter
@Setter
public class WrappedPacketOutScoreboardObjective extends WrappedPacket implements SendableWrapper {

    private String name = "";
    private String displayName = "";
    private HealthDisplayType healthDisplayType = HealthDisplayType.HEARTS;
    private Action action = Action.ADD;

    public WrappedPacketOutScoreboardObjective(Object packet) {
        super(packet);
    }

    public WrappedPacketOutScoreboardObjective(@NonNull String name, @NonNull String displayName, @NonNull HealthDisplayType healthDisplayType, @NonNull Action action) {
        super();
        this.name = name;
        this.displayName = displayName;
        this.healthDisplayType = healthDisplayType;
        this.action = action;
    }

    public WrappedPacketOutScoreboardObjective() {
        super();
    }

    @Override
    protected void setup() {

        this.name = readString(0);
        this.displayName = readString(1);
        this.healthDisplayType = MinecraftReflection.getHealthDisplayTypeConverter().getSpecific(readObject(0, MinecraftReflection.getHealthDisplayTypeClass()));
        this.action = Action.getById(readInt(0));

    }

    @Override
    public Object asNMSPacket() {
        return new PacketWrapper(PacketTypeClasses.Server.SCOREBOARD_OBJECTIVE)
                .setFieldByIndex(String.class, 0, this.name)
                .setFieldByIndex(String.class, 1, this.displayName)
                .setFieldByIndex(MinecraftReflection.getHealthDisplayTypeClass(), 0, MinecraftReflection.getHealthDisplayTypeConverter().getGeneric(this.healthDisplayType))
                .setFieldByIndex(int.class, 0, this.action.getId())
                .getPacket();
    }

    @Getter
    public enum Action {

        ADD(0),
        REMOVE(1),
        CHANGED(2);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        public static Action getById(int id) {
            for (Action action : values()) {
                if (action.id == id) {
                    return action;
                }
            }

            return null;
        }
    }

    public enum HealthDisplayType {
        INTEGER("integer"),
        HEARTS("hearts");

        private static final Map<String, HealthDisplayType> NAME_TO_TYPE_MAP;
        private final String name;

        HealthDisplayType(String var3) {
            this.name = var3;
        }

        public String getName() {
            return this.name;
        }

        public static HealthDisplayType getByName(String name) {
            return NAME_TO_TYPE_MAP.getOrDefault(name, INTEGER);
        }

        static {

            ImmutableMap.Builder<String, HealthDisplayType> builder = ImmutableMap.builder();

            for (HealthDisplayType displayType : values()) {
                builder.put(displayType.getName(), displayType);
            }

            NAME_TO_TYPE_MAP = builder.build();

        }
    }
}
