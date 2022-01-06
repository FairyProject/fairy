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

package io.fairyproject.bukkit.util.schematic;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import io.fairyproject.mc.util.BlockPosition;
import io.fairyproject.bukkit.util.schematic.impl.FAWESchematic;
import io.fairyproject.bukkit.util.schematic.impl.WorldEditSchematic;

import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;

@Getter
@Setter
public abstract class Schematic {

    private static final SchematicType TYPE;

    public static Schematic create(File file) {
        switch (Schematic.TYPE) {
//            case FAWE:
//                return new FAWESchematic(file);
            case WORLDEDIT:
                return new WorldEditSchematic(file);
        }

        throw new UnsupportedOperationException("Couldn't find SchematicType!");
    }

    public static Schematic create(File file, BlockPosition top, BlockPosition bottom) {
        switch (Schematic.TYPE) {
//            case FAWE:
//                return new FAWESchematic(file, top, bottom);
            case WORLDEDIT:
                return new WorldEditSchematic(file, top, bottom);
        }

        throw new UnsupportedOperationException("Couldn't find SchematicType!");
    }

    static {
        SchematicType lookupType = SchematicType.BUKKIT;

        lookup:
        {
//            try {
//                Class.forName("com.boydti.fawe.FaweAPI");
//                lookupType = SchematicType.FAWE;
//                break lookup;
//            } catch (ClassNotFoundException ex) {
//            }

            try {
                Class.forName("com.sk89q.worldedit.EditSession");
                lookupType = SchematicType.WORLDEDIT;
                break lookup;
            } catch (ClassNotFoundException ex) {
            }

        }

        TYPE = lookupType;
    }

    protected File file;
    protected BlockPosition top;
    protected BlockPosition bottom;

    public Schematic(File file) {
        this.file = file;
    }

    public Schematic(File file, BlockPosition top, BlockPosition bottom) {
        this.file = file;
        this.top = top;
        this.bottom = bottom;
    }

    public abstract void save(World world) throws IOException;

    public abstract void paste(Location location, int rotateX, int rotateY, int rotateZ) throws IOException;

    @Override
    public String toString() {
        return new StringJoiner(", ", Schematic.class.getSimpleName() + "[", "]")
                .add("file=" + file)
                .add("top=" + top)
                .add("bottom=" + bottom)
                .toString();
    }
}
