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

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.type.PacketTypeClasses;
import org.fairy.bukkit.packet.wrapper.SendableWrapper;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.reflection.minecraft.MinecraftVersion;
import org.fairy.bukkit.Imanity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@AutowiredWrappedPacket(value = PacketType.Server.ENTITY_VELOCITY, direction = PacketDirection.WRITE)
@Getter
public final class WrappedPacketOutEntityVelocity extends WrappedPacket implements SendableWrapper {
    private static Constructor<?> velocityConstructor, vec3dConstructor;
    private static Class<?> velocityClass, vec3dClass;
    private static boolean vec3dPresent;

    private int entityId;
    private double velocityX, velocityY, velocityZ;
    private Entity entity;

    public WrappedPacketOutEntityVelocity(final Object packet) {
        super(packet);
    }

    public WrappedPacketOutEntityVelocity(final Entity entity, final double velocityX, final double velocityY, final double velocityZ) {
        super();
        this.entityId = entity.getEntityId();
        this.entity = entity;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }

    /**
     * Velocity Constructor parameter types:
     * 1.7.10 -&gt; 1.13.2 use int, double, double, double style,
     * 1.14+ use int, Vec3D style
     */
    public static void init() {
        velocityClass = PacketTypeClasses.Server.ENTITY_VELOCITY;
        if (MinecraftVersion.getVersion().newerThan(MinecraftReflection.Version.v1_13_R2)) {
            try {
                vec3dClass = NMS_CLASS_RESOLVER.resolve("Vec3D");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            velocityConstructor = velocityClass.getConstructor(int.class, double.class, double.class, double.class);
        } catch (NoSuchMethodException e) {
            //That is fine, just a newer version
            try {
                velocityConstructor = velocityClass.getConstructor(int.class, vec3dClass);
                vec3dPresent = true;
                //vec3d constructor
                vec3dConstructor = vec3dClass.getConstructor(double.class, double.class, double.class);
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }

        }
    }

    @Override
    protected void setup() {
        //ENTITY ID
        this.entityId = readInt(0);

        int x = readInt(1);
        int y = readInt(2);
        int z = readInt(3);

        //VELOCITY XYZ
        this.velocityX = x / 8000.0D;
        this.velocityY = y / 8000.0D;
        this.velocityZ = z / 8000.0D;
    }

    /**
     * Lookup the associated entity by the ID that was sent in the packet.
     * @return Entity
     */
    public Entity getEntity() {
        if(entity != null) {
            return entity;
        }
        return entity = Imanity.IMPLEMENTATION.getEntity(this.entityId);
    }

    @Override
    public Object asNMSPacket() {
        if (!vec3dPresent) {
            try {
                return velocityConstructor.newInstance(entityId, velocityX, velocityY, velocityZ);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return velocityConstructor.newInstance(entityId, vec3dConstructor.newInstance(velocityX, velocityY, velocityZ));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
