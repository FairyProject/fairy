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

package io.fairyproject.bukkit.packet.wrapper.client.blockplace;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.wrapper.ObjectWrapper;
import io.fairyproject.mc.util.BlockPosition;

@Getter
final class WrappedPacketInBlockPlace_1_8 extends WrappedPacket {
    private static Class<?>
            BLOCK_POSITION,
            BLOCK_POSITION_SUPER;

    private BlockPosition blockPosition;
    private ItemStack itemStack;

    WrappedPacketInBlockPlace_1_8(final Object packet) {
        super(packet);
    }

    protected static void load() {
        try {
            BLOCK_POSITION = NMS_CLASS_RESOLVER.resolve("BlockPosition");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        BLOCK_POSITION_SUPER = BLOCK_POSITION.getSuperclass();
    }

    @Override
    protected void setup() {
        Object nmsBlockPos = packet.getPacketValueByIndex(BLOCK_POSITION, 0);

        this.blockPosition = new BlockPosition(0, 0, 0, this.getWorld().getName());

        ObjectWrapper objectWrapper = new ObjectWrapper(nmsBlockPos);

        this.blockPosition.setX(objectWrapper.invoke("getX"));
        this.blockPosition.setY(objectWrapper.invoke("getY"));
        this.blockPosition.setZ(objectWrapper.invoke("getZ"));

        this.itemStack = this.readItemStack(0);
    }
}
