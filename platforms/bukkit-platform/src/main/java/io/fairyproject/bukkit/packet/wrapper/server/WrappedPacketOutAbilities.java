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

import lombok.Getter;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.SendableWrapper;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@AutowiredWrappedPacket(value = PacketType.Server.ABILITIES, direction = PacketDirection.WRITE)
@Getter
public final class WrappedPacketOutAbilities extends WrappedPacket implements SendableWrapper {

    private static Class<?> PACKET_CLASS;
    private static Constructor<?> PACKET_CONSTRUCTOR;

    public static Class<?> PLAYER_ABILITIES_CLASS;
    public static Constructor<?> PLAYER_ABILITIES_CONSTRUCTOR;

    static {
        try {
            PLAYER_ABILITIES_CLASS = NMS_CLASS_RESOLVER.resolve("PlayerAbilities");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            PLAYER_ABILITIES_CONSTRUCTOR = PLAYER_ABILITIES_CLASS.getConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Object getPlayerAbilities(final boolean vulnerable, final boolean flying, final boolean allowFlight, final boolean canBuildInstantly,
                                            final float flySpeed, final float walkSpeed) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Object instance = PLAYER_ABILITIES_CONSTRUCTOR.newInstance();
        FieldResolver resolver = new FieldResolver(PLAYER_ABILITIES_CLASS);
        resolver.resolve(boolean.class, 0).set(instance, vulnerable);
        resolver.resolve(boolean.class, 1).set(instance, flying);
        resolver.resolve(boolean.class, 2).set(instance, allowFlight);
        resolver.resolve(boolean.class, 3).set(instance, canBuildInstantly);

        resolver.resolve(float.class, 0).set(instance, flySpeed);
        resolver.resolve(float.class, 1).set(instance, walkSpeed);
        return instance;
    }

    public static void init() {
        PACKET_CLASS = PacketTypeClasses.Server.ABILITIES;

        try {
            PACKET_CONSTRUCTOR = PACKET_CLASS.getConstructor(PLAYER_ABILITIES_CLASS);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private boolean vulnerable, flying, allowFlight, instantBuild;
    private float flySpeed, walkSpeed;

    public WrappedPacketOutAbilities(final Object packet) {
        super(packet);
    }

    public WrappedPacketOutAbilities(final boolean vulnerable,
                                     final boolean flying,
                                     final boolean allowFlight,
                                     final boolean canBuildInstantly,
                                     final float flySpeed,
                                     final float walkSpeed) {
        super();
        this.vulnerable = vulnerable;
        this.flying = flying;
        this.allowFlight = allowFlight;
        this.instantBuild = canBuildInstantly;
        this.flySpeed = flySpeed;
        this.walkSpeed = walkSpeed;
    }

    @Override
    protected void setup() {
        this.vulnerable = readBoolean(0);
        this.flying = readBoolean(1);
        this.allowFlight = readBoolean(2);
        this.instantBuild = readBoolean(3);

        this.flySpeed = readFloat(0);
        this.walkSpeed = readFloat(1);
    }

    @Override
    public Object asNMSPacket() {
        try {
            return PACKET_CONSTRUCTOR.newInstance(WrappedPacketOutAbilities.getPlayerAbilities(vulnerable, flying, allowFlight, instantBuild, flySpeed, walkSpeed));
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
