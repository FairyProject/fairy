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

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.Imanity;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.fairy.bukkit.reflection.minecraft.MinecraftVersion;

import javax.annotation.Nullable;
import java.util.Map;

@AutowiredWrappedPacket(value = PacketType.Client.ENTITY_ACTION, direction = PacketDirection.READ)
@Getter
public final class WrappedPacketInEntityAction extends WrappedPacket {

    private static Map<String, PlayerAction> CACHED_PLAYER_ACTION_NAMES;
    private static Map<Integer, PlayerAction> CACHED_PLAYER_ACTION_IDS;

    private static Class<?> entityActionClass;
    @Nullable
    private static Class<?> enumPlayerActionClass;
    private Entity entity;
    private int entityId;
    private PlayerAction action;
    private int jumpBoost;

    public WrappedPacketInEntityAction(final Object packet) {
        super(packet);
    }

    public static void init() {
        entityActionClass = NMS_CLASS_RESOLVER.resolveSilent("PacketPlayInEntityAction");
        if (!MinecraftVersion.VERSION.newerThan(MinecraftReflection.Version.v1_7_R4)) {
            try {
                enumPlayerActionClass = NMS_CLASS_RESOLVER.resolve(entityActionClass.getSimpleName() + "$EnumPlayerAction");
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        //All the already existing values

        ImmutableMap.Builder<String, PlayerAction> builder = ImmutableMap.builder();
        for (PlayerAction val : PlayerAction.values()) {
            builder.put(val.name(), val);
        }
        builder.put("PRESS_SHIFT_KEY", PlayerAction.START_SNEAKING);
        builder.put("RELEASE_SHIFT_KEY", PlayerAction.STOP_SNEAKING);
        builder.put("RIDING_JUMP", PlayerAction.START_RIDING_JUMP);
        CACHED_PLAYER_ACTION_NAMES = builder.build();

        ImmutableMap.Builder<Integer, PlayerAction> builderActionIds = ImmutableMap.builder();
        builderActionIds.put(1, PlayerAction.START_SNEAKING);
        builderActionIds.put(2, PlayerAction.STOP_SNEAKING);
        builderActionIds.put(3, PlayerAction.STOP_SLEEPING);
        builderActionIds.put(4, PlayerAction.START_SPRINTING);
        builderActionIds.put(5, PlayerAction.STOP_SPRINTING);
        builderActionIds.put(6, PlayerAction.START_RIDING_JUMP); //riding jump
        builderActionIds.put(7, PlayerAction.OPEN_INVENTORY); //horse interaction
        CACHED_PLAYER_ACTION_IDS = builderActionIds.build();

    }

    @Override
    protected void setup() {
        final int entityId = readInt(0);
        final int jumpBoost;
        if (MinecraftVersion.VERSION.olderThan(MinecraftReflection.Version.v1_8_R1)) {
            jumpBoost = readInt(2);
        } else {
            jumpBoost = readInt(1);
        }

        //1.7.10
        if (MinecraftVersion.VERSION.olderThan(MinecraftReflection.Version.v1_8_R1)) {
            int animationIndex = readInt(1);
            this.action = CACHED_PLAYER_ACTION_IDS.get(animationIndex);
        } else {
            final Object enumObj = readObject(0, enumPlayerActionClass);
            final String enumValueName = enumObj.toString();
            this.action = CACHED_PLAYER_ACTION_NAMES.get(enumValueName);
        }


        this.entityId = entityId;
        this.jumpBoost = jumpBoost;
    }

    /**
     * Lookup the associated entity by the ID that was sent in the packet.
     *
     * @return Entity
     */
    public Entity getEntity() {
        if(entity == null) {
            return entity = Imanity.IMPLEMENTATION.getEntity(this.getWorld(), this.entityId);
        }
        return entity;
    }

    public enum PlayerAction {
        START_SNEAKING,
        STOP_SNEAKING,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        START_RIDING_JUMP,
        STOP_RIDING_JUMP,
        OPEN_INVENTORY,
        START_FALL_FLYING
    }

}
