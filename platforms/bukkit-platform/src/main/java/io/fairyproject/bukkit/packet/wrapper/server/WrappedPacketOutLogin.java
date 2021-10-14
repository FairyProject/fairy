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
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.WorldType;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.SendableWrapper;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.wrapper.ConstructorWrapper;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

import javax.annotation.Nullable;

@AutowiredWrappedPacket(value = PacketType.Server.LOGIN, direction = PacketDirection.WRITE)
@Getter
@Setter
public class WrappedPacketOutLogin extends WrappedPacket implements SendableWrapper {

    private static Class<?> PACKET_CLASS, WORLD_TYPE_CLASS;
    private static Class<? extends Enum> ENUM_GAMEMODE_CLASS, ENUM_DIFFICULTY_CLASS;
    private static MethodWrapper NMS_WORLD_TYPE_GET_BY_NAME, NMS_WORLD_TYPE_NAME;
    private static ConstructorWrapper<?> PACKET_CONSTRUCTOR;

    private int playerId;
    private boolean hardcore;
    @Nullable private GameMode gameMode;
    private int dimension;
    private Difficulty difficulty;
    private int maxPlayers;
    private WorldType worldType;
    private boolean reducedDebugInfo;

    public WrappedPacketOutLogin(Object packet) {
        super(packet);
    }

    public WrappedPacketOutLogin() {
        super();
    }

    public WrappedPacketOutLogin(int playerId, boolean hardcore, GameMode gameMode, int dimension, Difficulty difficulty, int maxPlayers, WorldType worldType, boolean reducedDebugInfo) {
        super();
        this.playerId = playerId;
        this.hardcore = hardcore;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.maxPlayers = maxPlayers;
        this.worldType = worldType;
        this.reducedDebugInfo = reducedDebugInfo;
    }

    public static void init() {

        PACKET_CLASS = PacketTypeClasses.Server.LOGIN;

        try {
            ENUM_GAMEMODE_CLASS = MinecraftReflection.getEnumGamemodeClass();

            ENUM_DIFFICULTY_CLASS = NMS_CLASS_RESOLVER.resolve("EnumDifficulty");
            WORLD_TYPE_CLASS = NMS_CLASS_RESOLVER.resolve("WorldType");

            MethodResolver methodResolver = new MethodResolver(WORLD_TYPE_CLASS);
            NMS_WORLD_TYPE_GET_BY_NAME = methodResolver.resolve(WORLD_TYPE_CLASS, 0, String.class);
            NMS_WORLD_TYPE_NAME = methodResolver.resolve(String.class, 0);

            PACKET_CONSTRUCTOR = new ConstructorWrapper<>(PACKET_CLASS.getDeclaredConstructor(
                    int.class,
                    ENUM_GAMEMODE_CLASS,
                    boolean.class,
                    int.class,
                    ENUM_DIFFICULTY_CLASS,
                    int.class,
                    WORLD_TYPE_CLASS,
                    boolean.class
            ));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    @Override
    protected void setup() {
        this.playerId = readInt(0);

        this.hardcore = readBoolean(0);

        this.gameMode = MinecraftReflection.getGameModeConverter().getSpecific(readObject(0, ENUM_GAMEMODE_CLASS));

        this.dimension = readInt(1);

        Enum difficultyEnum = readObject(0, ENUM_DIFFICULTY_CLASS);
        if (difficultyEnum != null) {
            this.difficulty = Difficulty.valueOf(difficultyEnum.name());
        }

        Object worldType = readObject(0, WORLD_TYPE_CLASS);
        if (worldType != null) {
            this.worldType = WorldType.getByName((String) NMS_WORLD_TYPE_NAME.invoke(worldType));
        }

        this.maxPlayers = readInt(2);

        this.reducedDebugInfo = readBoolean(0);
    }

    @Override
    public Object asNMSPacket() {
        return PACKET_CONSTRUCTOR.newInstance(
                this.playerId,
                MinecraftReflection.getGameModeConverter().getGeneric(this.gameMode),
                this.hardcore,
                this.dimension,
                Enum.valueOf(ENUM_DIFFICULTY_CLASS, this.difficulty.name()),
                this.maxPlayers,
                NMS_WORLD_TYPE_GET_BY_NAME.invoke(null, this.worldType.getName()),
                this.reducedDebugInfo
        );
    }
}
