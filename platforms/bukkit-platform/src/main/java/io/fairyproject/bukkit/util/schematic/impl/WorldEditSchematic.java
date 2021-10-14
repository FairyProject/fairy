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

package io.fairyproject.bukkit.util.schematic.impl;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.WorldData;
import io.fairyproject.bukkit.util.schematic.Schematic;
import io.fairyproject.mc.util.BlockPosition;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WorldEditSchematic extends Schematic {

    public WorldEditSchematic(File file) {
        super(file);
    }

    public WorldEditSchematic(File file, BlockPosition top, BlockPosition bottom) {
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
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        try (ClipboardWriter writer = ClipboardFormat.SCHEMATIC.getWriter(new FileOutputStream(this.file))) {
            writer.write(clipboard, new BukkitWorld(world).getWorldData());
        }
    }

    @Override
    public void paste(Location location, int rotateX, int rotateY, int rotateZ) throws IOException {
        Preconditions.checkNotNull(location);
        Preconditions.checkNotNull(this.file);

        Vector vector = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        World world = new BukkitWorld(location.getWorld());
        WorldData worldData = world.getWorldData();
        Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(this.file)).read(worldData);
        Region region = clipboard.getRegion();

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        AffineTransform transform = new AffineTransform();

        if (rotateX != 0) transform = transform.rotateX(rotateX);
        if (rotateY != 0) transform = transform.rotateY(rotateY);
        if (rotateZ != 0) transform = transform.rotateZ(rotateZ);

        ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, region, clipboard.getOrigin(), editSession, vector);
        if (!transform.isIdentity()) copy.setTransform(transform);

        Vector top = region.getMaximumPoint();
        Vector bottom = region.getMinimumPoint();

        this.top = new BlockPosition(top.getBlockX(), top.getBlockY(), top.getBlockZ(), location.getWorld().getName());
        this.bottom = new BlockPosition(bottom.getBlockX(), bottom.getBlockY(), bottom.getBlockZ(), location.getWorld().getName());

        try {
            Operations.completeLegacy(copy);
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
        editSession.flushQueue();
    }
}
