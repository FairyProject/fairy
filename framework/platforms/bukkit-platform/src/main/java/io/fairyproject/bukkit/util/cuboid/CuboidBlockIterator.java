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

package io.fairyproject.bukkit.util.cuboid;

import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Iterator;

class CuboidBlockIterator
        implements Iterator<Block> {
    private final World world;
    private final int baseX;
    private final int baseY;
    private final int baseZ;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private int x;
    private int y;
    private int z;

    public CuboidBlockIterator(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = world;
        this.baseX = x1;
        this.baseY = y1;
        this.baseZ = z1;
        this.sizeX = Math.abs(x2 - x1) + 1;
        this.sizeY = Math.abs(y2 - y1) + 1;
        this.sizeZ = Math.abs(z2 - z1) + 1;
        this.z = 0;
        this.y = 0;
        this.x = 0;
    }

    @Override
	public boolean hasNext() {
        return (this.x < this.sizeX) && (this.y < this.sizeY) && (this.z < this.sizeZ);
    }

    @Override
	public Block next() {
        final Block block = this.world.getBlockAt(this.baseX + this.x, this.baseY + this.y, this.baseZ + this.z);
        if (++this.x >= this.sizeX) {
            this.x = 0;
            if (++this.y >= this.sizeY) {
                this.y = 0;
                this.z += 1;
            }
        }
        return block;
    }

    @Override
	public void remove()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
