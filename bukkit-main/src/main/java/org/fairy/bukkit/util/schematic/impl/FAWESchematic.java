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

package org.fairy.bukkit.util.schematic.impl;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.fairy.bukkit.util.BlockPosition;

import java.io.File;
import java.io.IOException;

public class FAWESchematic extends WorldEditSchematic {

    public FAWESchematic(File file) {
        super(file);
    }

    public FAWESchematic(File file, BlockPosition top, BlockPosition bottom) {
        super(file, top, bottom);
    }

    @Override
    public void save(org.bukkit.World world) throws IOException {
        Preconditions.checkNotNull(this.file);
        Preconditions.checkNotNull(this.top);
        Preconditions.checkNotNull(this.bottom);

        Vector top = new Vector(this.top.getX(), this.top.getY(), this.top.getZ());
        Vector bottom = new Vector(this.bottom.getX(), this.bottom.getY(), this.bottom.getZ());

        CuboidRegion region = new CuboidRegion(new BukkitWorld(world), top, bottom);
        com.boydti.fawe.object.schematic.Schematic schematic = new com.boydti.fawe.object.schematic.Schematic(region);
        schematic.save(this.file, ClipboardFormat.SCHEMATIC);
    }
}
