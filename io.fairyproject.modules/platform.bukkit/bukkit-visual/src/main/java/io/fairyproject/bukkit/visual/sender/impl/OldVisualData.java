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
import io.fairyproject.Debug;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.bukkit.visual.sender.VisualData;

import java.lang.reflect.Method;

public class OldVisualData implements VisualData {

    private final MethodWrapper<?> blockGetByIdMethod;
    private final MethodWrapper<?> fromLegacyDataMethod;
    private final MethodWrapper<?> fromIdMethod;
    private final Object blockRegistry;

    public OldVisualData(BukkitNMSManager nmsManager) {
        MethodWrapper<?> blockGetById = null;
        MethodWrapper<?> fromLegacyData = null;
        MethodWrapper<?> fromId = null;
        Object blockRegistry = null;

        try {
            if (nmsManager.isSupported()) {
                final Class<?> blockType = nmsManager.getNmsClassResolver().resolve("Block");
                Class<?> registryID;
                try {
                    registryID = nmsManager.getNmsClassResolver().resolve("RegistryBlockID");
                } catch (ClassNotFoundException ex) {
                    registryID = nmsManager.getNmsClassResolver().resolve("RegistryID");
                }
                blockGetById = new MethodWrapper<>(blockType.getMethod("getById", int.class));
                fromLegacyData = new MethodWrapper<>(blockType.getMethod("fromLegacyData", int.class));
                blockRegistry = new FieldResolver(blockType).resolve(registryID, 0).get(0);
                for (Method method : registryID.getDeclaredMethods()) {
                    if (method.getReturnType() == int.class && method.getParameterCount() == 1) {
                        fromId = new MethodWrapper<>(method);
                        break;
                    }
                }

                if (fromId == null)
                    throw new IllegalArgumentException("Cannot find method 'fromId' in " + registryID.getName());

                Debug.log("Initialized OldData for Visual module.");
            }
        } catch (Exception ex) {
        }

        blockGetByIdMethod = blockGetById;
        fromLegacyDataMethod = fromLegacyData;
        fromIdMethod = fromId;
        this.blockRegistry = blockRegistry;
    }

    public boolean isCapable() {
        return blockGetByIdMethod != null &&
                fromLegacyDataMethod != null &&
                fromIdMethod != null &&
                blockRegistry != null;
    }

    public int getId(XMaterial material) {
        Object block = blockGetByIdMethod.invoke(null, material.getId());
        Object blockData = fromLegacyDataMethod.invoke(block, material.getData());
        final Object invoke = fromIdMethod.invoke(blockRegistry, blockData);
        return (int) invoke;
    }

}
