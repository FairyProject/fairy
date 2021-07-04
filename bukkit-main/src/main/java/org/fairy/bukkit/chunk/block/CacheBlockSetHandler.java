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

package org.fairy.bukkit.chunk.block;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.fairy.Fairy;
import org.fairy.bukkit.chunk.block.location.YLocationFixed;
import org.fairy.bukkit.chunk.block.location.YLocationHighest;
import org.fairy.metadata.MetadataKey;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class CacheBlockSetHandler {

    public static final MetadataKey<CacheBlockSetHandler> METADATA = MetadataKey.create(Fairy.METADATA_PREFIX + "BlockSetHandler", CacheBlockSetHandler.class);

    private final World world;
    private Map<Long, CacheChunkChanges> cachedChanges = new HashMap<>();

    public void setTypeAtHighest(Location location, Material material) {
        this.setTypeAtHighest(location.getBlockX(), location.getBlockZ(), material);
    }

    public void setTypeAtHighest(Location location, Material material, byte data) {
        this.setTypeAtHighest(location.getBlockX(), location.getBlockZ(), material, data);
    }

    public void setTypeAtHighest(int x, int z, Material material) {
        this.setTypeAtHighest(x, z, material, (byte) 0);
    }

    public void setTypeAtHighest(int x, int z, Material material, byte data) {
        this.setType(new CacheBlockChange(x, new YLocationHighest(), z, material, data));
    }

    public void setType(Location location, Material material) {
        this.setType(location.getBlockX(), location.getBlockY(), location.getBlockZ(), material);
    }

    public void setType(Location location, Material material, byte data) {
        this.setType(location.getBlockX(), location.getBlockY(), location.getBlockZ(), material, data);
    }

    public void setType(int x, int y, int z, Material material) {
        this.setType(x, y, z, material, (byte) 0);
    }

    public void setType(int x, int y, int z, Material material, byte data) {
        this.setType(new CacheBlockChange(x, new YLocationFixed(y), z, material, data));
    }

    public void setType(CacheBlockChange blockChange) {
        if (world.isChunkLoaded(blockChange.getX() >> 4, blockChange.getZ() >> 4)) {

            Chunk chunk = ((CraftChunk) world.getChunkAt(blockChange.getX() >> 4, blockChange.getZ() >> 4)).getHandle();

            final int combined = blockChange.getMaterial().getId() + (blockChange.getData() << 12);
            final IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);

            int y = blockChange.getY().get(blockChange.getX(), blockChange.getZ(), chunk);
            BlockPosition blockPosition = new BlockPosition(blockChange.getX(), y, blockChange.getZ());
            chunk.a(blockPosition, ibd);

            chunk.world.notify(blockPosition);
            return;

        }

        long key = LongHash.toLong(blockChange.getX() >> 4, blockChange.getZ() >> 4);
        if (cachedChanges.containsKey(key)) {
            cachedChanges.get(key).add(blockChange);
            return;
        }

        CacheChunkChanges chunkChanges = new CacheChunkChanges();
        cachedChanges.put(key, chunkChanges);
        chunkChanges.add(blockChange);
    }

    protected void placeIfExists(org.bukkit.Chunk chunk) {

        long key = LongHash.toLong(chunk.getX(), chunk.getZ());
        CacheChunkChanges chunkChanges = this.cachedChanges.get(key);
        if (chunkChanges != null) {
            chunkChanges.place(((CraftChunk) chunk).getHandle());
            chunkChanges.free();
            this.cachedChanges.remove(key);
        }

    }

}
