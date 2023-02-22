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

package io.fairyproject.bukkit.mc.impl;

import io.fairyproject.bukkit.mc.EntityUUIDFinder;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
public class EntityUUIDFinderImpl implements EntityUUIDFinder {

    private final BukkitNMSManager nmsManager;
    private Function<UUID, Entity> uuidToEntity;

    @Override
    public Entity findEntityByUuid(UUID entityUuid) {
        if (uuidToEntity == null) {
            Function<UUID, Entity> uuidToEntity;
            try {
                Bukkit.class.getDeclaredMethod("getEntity", UUID.class);
                uuidToEntity = Bukkit::getEntity;
            } catch (NoSuchMethodException ex) {
                try {
                    final Class<?> minecraftServer = this.nmsManager.getNmsClassResolver().resolve("server.MinecraftServer","MinecraftServer");
                    final Object server = minecraftServer.getMethod("getServer").invoke(null);
                    Method method = minecraftServer.getDeclaredMethod("a", UUID.class);
                    MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);

                    uuidToEntity = uuid -> {
                        try {
                            return MinecraftReflection.getBukkitEntity(methodHandle.invoke(server, uuid));
                        } catch (Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }
                    };
                } catch (Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }
            }
            this.uuidToEntity = uuidToEntity;
        }

        return this.uuidToEntity.apply(entityUuid);
    }
}
