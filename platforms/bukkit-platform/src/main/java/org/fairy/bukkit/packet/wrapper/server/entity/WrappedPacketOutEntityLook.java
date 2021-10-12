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

package org.fairy.bukkit.packet.wrapper.server.entity;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.fairy.bukkit.Imanity;

@AutowiredWrappedPacket(value = PacketType.Server.ENTITY_LOOK, direction = PacketDirection.WRITE)
@Getter
public class WrappedPacketOutEntityLook extends WrappedPacket {
    private int entityID;
    private Entity entity;
    private byte yaw, pitch;
    private boolean onGround;

    public WrappedPacketOutEntityLook(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {
        entityID = readInt(0);
        onGround = readBoolean(0);
        switch (EntityPacketUtil.getMode()) {
            case 0:
                yaw = readByte(3);
                pitch = readByte(4);
                break;
            case 1:
            case 2:
                yaw = readByte(0);
                pitch = readByte(1);
                break;
        }
    }

    /**
     * Lookup the associated entity by the ID that was sent in the packet.
     *
     * @return Entity
     */
    public Entity getEntity() {
        if (entity != null) {
            return entity;
        }
        return entity = Imanity.IMPLEMENTATION.getEntity(this.entityID);
    }

}