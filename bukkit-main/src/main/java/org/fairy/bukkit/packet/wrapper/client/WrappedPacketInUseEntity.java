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

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.fairy.bukkit.Imanity;

@Getter
@AutowiredWrappedPacket(value = PacketType.Client.USE_ENTITY, direction = PacketDirection.READ)
public final class WrappedPacketInUseEntity extends WrappedPacket {

    private static Class<?> ENUM_ENTITY_USE_ACTION;

    private Entity entity;
    private int entityID;
    private EntityUseAction action;
    public WrappedPacketInUseEntity(final Object packet) {
        super(packet);
    }

    public static void init() {
        Class<?> useEntityClass = NMS_CLASS_RESOLVER.resolveSilent("PacketPlayInUseEntity");
        try {
            ENUM_ENTITY_USE_ACTION = NMS_CLASS_RESOLVER.resolve("EnumEntityUseAction");
        } catch (ClassNotFoundException e) {
            //That is fine, it is probably a subclass
            ENUM_ENTITY_USE_ACTION = NMS_CLASS_RESOLVER.resolveSilent(useEntityClass.getSimpleName() + "$EnumEntityUseAction");
        }
    }

    @Override
    protected void setup() {
        this.entityID = readInt(0);
        if(ENUM_ENTITY_USE_ACTION == null) {
                System.out.println("class is null");
        }
        final Object useActionEnum = readObject(0, ENUM_ENTITY_USE_ACTION);
        this.action = EntityUseAction.valueOf(useActionEnum.toString());
    }

    /**
     * Lookup the associated entity by the ID that was sent in the packet.
     * @return Entity
     */
    public Entity getEntity() {
        if(entity != null) {
            return entity;
        }
        return entity = Imanity.IMPLEMENTATION.getEntity(this.getWorld(), this.entityID);
    }

    public enum EntityUseAction {
        INTERACT, INTERACT_AT, ATTACK, INVALID
    }
}
