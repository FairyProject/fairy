/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.bukkit.visual.sender.impl;

import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.fairyproject.bukkit.visual.sender.VisualData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public class BukkitVisualData implements VisualData {
    @Override
    public boolean isCapable() {
        try {
            Class<?> aClass = Class.forName("org.bukkit.block.data.BlockData");
            aClass.getDeclaredMethod("getAsString");

            return true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public int getId(@NotNull XMaterial material) {
        Material bukkitMaterial = material.parseMaterial();
        if (bukkitMaterial == null) return 0;

        BlockData blockData = Bukkit.createBlockData(bukkitMaterial);
        String string = blockData.getAsString();
        WrappedBlockState wrappedBlockState = WrappedBlockState.getByString(string);

        return wrappedBlockState.getGlobalId();
    }
}
