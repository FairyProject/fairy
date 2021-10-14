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

import io.fairyproject.bukkit.reflection.minecraft.DataWatcher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.SendableWrapper;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.wrapper.DataWatcherWrapper;
import io.fairyproject.bukkit.reflection.wrapper.PacketWrapper;
import io.fairyproject.bukkit.reflection.wrapper.WatchableObjectWrapper;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import io.fairyproject.util.collection.ConvertedList;

import java.util.List;
import java.util.stream.Collectors;

@AutowiredWrappedPacket(value = PacketType.Server.SPAWN_ENTITY_LIVING, direction = PacketDirection.WRITE)
@Getter
@Setter
@AllArgsConstructor
@Builder
public class WrappedPacketOutSpawnEntityLiving extends WrappedPacket implements SendableWrapper {
    public WrappedPacketOutSpawnEntityLiving(Object packet) {
        super(packet);
    }

    private int entityId, entityTypeId;
    private int locX, locY, locZ;

    private float yaw, pitch, headPitch;

    private double velX, velY, velZ;

    private DataWatcherWrapper dataWatcher;
    private ConvertedList<Object, WatchableObjectWrapper> watchableObjects;

    @Override
    protected void setup() {
        this.entityId = readInt(0);
        this.entityTypeId = readInt(1);

        this.locX = readInt(2) / 32;
        this.locY = readInt(3) / 32;
        this.locZ = readInt(4) / 32;

        this.yaw = readByte(0) / 256.0F * 360.0F;
        this.pitch = readByte(1) / 256.0F * 360.0F;
        this.headPitch = readByte(2) / 256.0F * 360.0F;

        this.velX = readInt(5) / 8000.0D;
        this.velY = readInt(6) / 8000.0D;
        this.velZ = readInt(7) / 8000.0D;

        this.dataWatcher = new DataWatcherWrapper(readObject(0, DataWatcher.TYPE));
        this.watchableObjects = new ConvertedList<Object, WatchableObjectWrapper>(readList(0)) {
            @Override
            protected WatchableObjectWrapper toOuter(Object o) {
                return WatchableObjectWrapper.getConverter().getSpecific(o);
            }

            @Override
            protected Object toInner(WatchableObjectWrapper watchableObjectWrapper) {
                return WatchableObjectWrapper.getConverter().getGeneric(watchableObjectWrapper);
            }
        };
    }

    @Override
    public Object asNMSPacket() {
        PacketWrapper packetWrapper = new PacketWrapper(PacketTypeClasses.Server.SPAWN_ENTITY_LIVING)
                .setFieldByIndex(int.class, 0, this.entityId)
                .setFieldByIndex(int.class, 1, this.entityTypeId)
                .setFieldByIndex(int.class, 2, (int) Math.floor(this.locX * 32.0D))
                .setFieldByIndex(int.class, 3, (int) Math.floor(this.locY * 32.0D))
                .setFieldByIndex(int.class, 4, (int) Math.floor(this.locZ * 32.0D))
                .setFieldByIndex(byte.class, 0, (byte) ((int)(this.yaw * 256.0F / 360.0F)))
                .setFieldByIndex(byte.class, 1, (byte) ((int)(this.pitch * 256.0F / 360.0F)))
                .setFieldByIndex(byte.class, 2, (byte) ((int)(this.headPitch * 256.0F / 360.0F)))
                .setFieldByIndex(int.class, 5, (int) (this.velX * 8000.0D))
                .setFieldByIndex(int.class, 6, (int) (this.velY * 8000.0D))
                .setFieldByIndex(int.class, 7, (int) (this.velZ * 8000.0D))
                .setFieldByIndex(DataWatcher.TYPE, 0, this.dataWatcher.getDataWatcherObject());
        if (this.watchableObjects != null) {
            packetWrapper.setFieldByIndex(List.class, 0, this.watchableObjects.stream().map(watchableObjectWrapper -> WatchableObjectWrapper.getConverter().getGeneric(watchableObjectWrapper)).collect(Collectors.toList()));
        }

        return packetWrapper.getPacket();
    }

}
