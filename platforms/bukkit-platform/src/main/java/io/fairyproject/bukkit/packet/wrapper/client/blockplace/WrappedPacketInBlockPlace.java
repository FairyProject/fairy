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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import io.fairyproject.mc.util.BlockPosition;
import io.fairyproject.bukkit.util.MinecraftVersion;

@Getter
@AutowiredWrappedPacket(value = PacketType.Client.BLOCK_PLACE, direction = PacketDirection.READ)
public final class WrappedPacketInBlockPlace extends WrappedPacket {

    private BlockPosition blockPosition;
    private ItemStack itemStack;

    public WrappedPacketInBlockPlace(final Player player, final Object packet) {
        super(player, packet);
    }

    public static void load() {
        if (MinecraftVersion.newerThan(MinecraftVersion.V.v1_7)) {
            WrappedPacketInBlockPlace_1_8.load();
        }
    }

    @Override
    protected void setup() {
        //1.7.10
        BlockPosition position = null;
        ItemStack itemStack = null;
        try {

            if (MinecraftVersion.newerThan(MinecraftVersion.V.v1_8)) {

                final WrappedPacketInBlockPlace_1_9 blockPlace_1_9 = new WrappedPacketInBlockPlace_1_9(getPlayer(), packet);
                final Block block = blockPlace_1_9.getBlock();
                position = new BlockPosition(block.getX(), block.getY(), block.getZ(), this.getWorld().getName());
                itemStack = new ItemStack(block.getType());

            } else if (MinecraftVersion.newerThan(MinecraftVersion.V.v1_7)) {

                final WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
                position = blockPlace_1_8.getBlockPosition();
                itemStack = blockPlace_1_8.getItemStack();

            } else {
                final WrappedPacketInBlockPlace_1_7_10 blockPlace_1_7_10 = new WrappedPacketInBlockPlace_1_7_10(getPlayer(), packet);
                position = blockPlace_1_7_10.getBlockPosition();
                itemStack = blockPlace_1_7_10.getItemStack();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        this.blockPosition = position;
        this.itemStack = itemStack;
    }

}
