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

package io.fairyproject.bukkit.packet.wrapper.server.entity;

import lombok.Getter;
import org.bukkit.entity.Entity;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import io.fairyproject.bukkit.Imanity;

@AutowiredWrappedPacket(value = PacketType.Server.ENTITY, direction = PacketDirection.WRITE)
@Getter
public class WrappedPacketOutEntity extends WrappedPacket {

    private Entity entity;
    private int entityID;
    private double deltaX, deltaY, deltaZ;
    private byte yaw, pitch;
    private boolean onGround;

    public WrappedPacketOutEntity(Object packet) {
        super(packet);
    }

    public static void init() {
        EntityPacketUtil.init();
    }

    @Override
    protected void setup() {
        this.entityID = readInt(0);
        this.onGround = readBoolean(0);
        int dX = 1, dY = 1, dZ = 1;
        switch (EntityPacketUtil.getMode()) {
            case 0:
                dX = readByte(0);
                dY = readByte(1);
                dZ = readByte(2);
                this.yaw = readByte(3);
                this.pitch = readByte(4);
                break;
            case 1:
                dX = readInt(1);
                dY = readInt(2);
                dZ = readInt(3);
                this.yaw = readByte(0);
                this.pitch = readByte(1);
                break;
            case 2:
                dX = readShort(0);
                dY = readShort(1);
                dZ = readShort(2);
                this.yaw = readByte(0);
                this.pitch = readByte(1);
                break;
        }
        this.deltaX = dX / EntityPacketUtil.getDXYZDivisor();
        this.deltaY = dY / EntityPacketUtil.getDXYZDivisor();
        this.deltaZ = dZ / EntityPacketUtil.getDXYZDivisor();
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
