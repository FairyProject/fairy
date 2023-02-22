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
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.bukkit.visual.sender.VisualData;
import org.bukkit.Material;

public class NewVisualData implements VisualData {

    private final Class<?> magicNumbersClass;
    private final MethodWrapper<?> fromLegacyDataMethod;
    private final MethodWrapper<?> getIdMethod;

    public NewVisualData(BukkitNMSManager nmsManager) {
        Class<?> magicNumbers = null;
        MethodWrapper<?> fromLegacyData = null;
        MethodWrapper<?> getId = null;

        try {
            if (nmsManager.isSupported()) {
                final Class<?> blockStateType = nmsManager.getNmsClassResolver().resolve("world.level.block.state.BlockState", "IBlockData");
                final Class<?> blockType = nmsManager.getNmsClassResolver().resolve("world.level.block.Block", "Block");
                magicNumbers = nmsManager.getObcClassResolver().resolve("util.CraftMagicNumbers");
                fromLegacyData = new MethodResolver(magicNumbers).resolve(blockStateType, 0, Material.class, byte.class);
                getId = new MethodResolver(blockType).resolve(blockStateType, 0, blockType);

                System.out.println("Initialized NewData for Visual module.");
            }
        } catch (ReflectiveOperationException ignored) {
        }

        magicNumbersClass = magicNumbers;
        fromLegacyDataMethod = fromLegacyData;
        getIdMethod = getId;
    }

    @Override
    public int getId(XMaterial material) {
        final Object blockState = fromLegacyDataMethod.invoke(null, material.parseMaterial(), material.getData());
        final Object invoke = getIdMethod.invoke(null, blockState);
        return (int) invoke;
    }

    @Override
    public boolean isCapable() {
        return magicNumbersClass != null && fromLegacyDataMethod != null && getIdMethod != null;
    }
}
