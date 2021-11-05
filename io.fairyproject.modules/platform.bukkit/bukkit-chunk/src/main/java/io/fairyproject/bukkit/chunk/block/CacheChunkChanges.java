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

package io.fairyproject.bukkit.chunk.block;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.IBlockData;

import java.util.ArrayList;
import java.util.List;

public class CacheChunkChanges {

    private List<CacheBlockChange> blockChanges = new ArrayList<>();

    public void add(CacheBlockChange blockChange) {
        this.blockChanges.add(blockChange);
    }

    public void place(Chunk chunk) {
        BlockPosition.MutableBlockPosition blockPosition = new BlockPosition.MutableBlockPosition();

        for (CacheBlockChange blockChange : this.blockChanges) {
            final int combined = blockChange.getMaterial().getId() + (blockChange.getData() << 12);
            final IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);

            int y = blockChange.getY().get(blockChange.getX(), blockChange.getZ(), chunk);

            chunk.a(blockPosition.c(blockChange.getX(), y, blockChange.getZ()), ibd);
            chunk.world.notify(blockPosition);
        }

    }

    public void free() {
        blockChanges.clear();
        blockChanges = null;
    }

}
