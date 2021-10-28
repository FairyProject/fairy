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

package io.fairyproject.bukkit.packet.wrapper.client;

import io.fairyproject.bukkit.packet.wrapper.other.EnumDirection;
import lombok.Getter;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.wrapper.ObjectWrapper;
import io.fairyproject.mc.util.BlockPosition;
import io.fairyproject.bukkit.util.MinecraftVersion;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@Getter
@AutowiredWrappedPacket(value = PacketType.Client.BLOCK_DIG, direction = PacketDirection.READ)
public final class WrappedPacketInBlockDig extends WrappedPacket {
    private static Class<?> blockDigClass, blockPositionClass, enumDirectionClass, digTypeClass;
    private BlockPosition blockPosition;
    private EnumDirection direction;
    private PlayerDigType digType;
    public WrappedPacketInBlockDig(Object packet) {
        super(packet);
    }

    public static void init() {
        blockDigClass = PacketTypeClasses.Client.BLOCK_DIG;
        try {
            if (MinecraftVersion.newerThan(MinecraftVersion.V.v1_7)) {
                blockPositionClass = NMS_CLASS_RESOLVER.resolve("BlockPosition");
                enumDirectionClass = NMS_CLASS_RESOLVER.resolve("EnumDirection");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (!MinecraftVersion.equals(MinecraftVersion.V.v1_7)) {
            try {
                digTypeClass = NMS_CLASS_RESOLVER.resolve("EnumPlayerDigType");
            } catch (ClassNotFoundException e) {
                //It is probably a subclass
                digTypeClass = NMS_CLASS_RESOLVER.resolveSilent(blockDigClass.getSimpleName() + "$EnumPlayerDigType");
            }
        }
    }

    @Override
    protected void setup() {
        EnumDirection enumDirection = null;
        PlayerDigType enumDigType = null;
        int x = 0, y = 0, z = 0;
        //1.7.10
        if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_8)) {
            enumDigType = PlayerDigType.values()[new FieldResolver(blockDigClass).resolve(int.class, 4).get(null)];

            x = readInt(0);
            y = readInt(1);
            z = readInt(2);

            enumDirection = null;
        } else {
            //1.8+
            final Object blockPosObj = readObject(0, blockPositionClass);
            final Enum<?> enumDirectionObj = (Enum<?>) readObject(0, enumDirectionClass);
            final Enum<?> digTypeObj = (Enum<?>) readObject(0, digTypeClass);

            ObjectWrapper objectWrapper = new ObjectWrapper(blockPosObj);
            x = objectWrapper.getFieldByIndex(int.class, 0);
            y = objectWrapper.getFieldByIndex(int.class, 1);
            z = objectWrapper.getFieldByIndex(int.class, 2);

            enumDirection = EnumDirection.valueOf(enumDirectionObj.name());
            enumDigType = PlayerDigType.valueOf(digTypeObj.name());
        }
        this.blockPosition = new BlockPosition(x, y, z);
        if (enumDirection == null) {
            this.direction = EnumDirection.NULL;
        } else {
            this.direction = enumDirection;
        }
        this.digType = enumDigType;
    }

    public enum PlayerDigType {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_HELD_ITEMS,
        SWAP_ITEM_WITH_OFFHAND,
        WRONG_PACKET
    }


}
