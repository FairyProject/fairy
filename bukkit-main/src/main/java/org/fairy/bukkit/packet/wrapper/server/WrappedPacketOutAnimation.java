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

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.type.PacketTypeClasses;
import org.fairy.bukkit.packet.wrapper.SendableWrapper;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.Imanity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@AutowiredWrappedPacket(value = PacketType.Server.ANIMATION, direction = PacketDirection.WRITE)
@Getter
public final class WrappedPacketOutAnimation extends WrappedPacket implements SendableWrapper {

    private static Class<?> animationClass, nmsEntityClass;
    private static Constructor<?> animationConstructor;
    private static Map<Integer, EntityAnimationType> cachedAnimationIDS;
    private static Map<EntityAnimationType, Integer> cachedAnimations;

    private Entity entity;
    private int entityID;
    private EntityAnimationType type;

    public WrappedPacketOutAnimation(final Object packet) {
        super(packet);
    }

    public WrappedPacketOutAnimation(final Entity target, final EntityAnimationType type) {
        super();
        this.entityID = target.getEntityId();
        this.entity = target;
        this.type = type;
    }

    public static void init() {

        animationClass = PacketTypeClasses.Server.ANIMATION;
        try {
            nmsEntityClass = NMS_CLASS_RESOLVER.resolve("Entity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            animationConstructor = animationClass.getConstructor(nmsEntityClass, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        cachedAnimationIDS = ImmutableMap.<Integer, EntityAnimationType>builder()
                .put(0, EntityAnimationType.SWING_MAIN_ARM)
                .put(1, EntityAnimationType.TAKE_DAMAGE)
                .put(2, EntityAnimationType.LEAVE_BED)
                .put(3, EntityAnimationType.SWING_OFFHAND)
                .put(4, EntityAnimationType.CRITICAL_EFFECT)
                .put(5, EntityAnimationType.MAGIC_CRITICAL_EFFECT)
                .build();

        cachedAnimations = ImmutableMap.<EntityAnimationType, Integer>builder()
                .put(EntityAnimationType.SWING_MAIN_ARM, 0)
                .put(EntityAnimationType.TAKE_DAMAGE, 1)
                .put(EntityAnimationType.LEAVE_BED, 2)
                .put(EntityAnimationType.SWING_OFFHAND, 3)
                .put(EntityAnimationType.CRITICAL_EFFECT, 4)
                .put(EntityAnimationType.MAGIC_CRITICAL_EFFECT, 5)
                .build();
    }

    @Override
    protected void setup() {
        this.entityID = readInt(0);
        int animationID = readInt(1);
        this.type = cachedAnimationIDS.get(animationID);
    }

    /**
     * Lookup the associated entity by the ID that was sent in the packet.
     *
     * @return Entity
     */
    public Entity getEntity() {
        return Imanity.IMPLEMENTATION.getEntity(this.entityID);
    }

    @Override
    public Object asNMSPacket() {
        final Object nmsEntity = MinecraftReflection.getHandleSilent(this.entity);
        final int index = cachedAnimations.get(type);
        try {
            return animationConstructor.newInstance(nmsEntity, index);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public enum EntityAnimationType {
        SWING_MAIN_ARM, TAKE_DAMAGE, LEAVE_BED, SWING_OFFHAND, CRITICAL_EFFECT, MAGIC_CRITICAL_EFFECT
    }
}
