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

package io.fairyproject.bukkit.impl.server;

import io.fairyproject.bukkit.impl.annotation.ServerImpl;
import io.fairyproject.bukkit.player.movement.MovementListener;
import io.fairyproject.bukkit.player.movement.impl.AbstractMovementImplementation;
import io.fairyproject.bukkit.player.movement.impl.BukkitMovementImplementation;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.ResolverQuery;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.bukkit.reflection.wrapper.ObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * TODO
 * - Just remove this tbh
 */
@ServerImpl
public class NormalImplementation implements ServerImplementation {

    private static final ObjectWrapper MINECRAFT_SERVER;
    private static final MethodWrapper<?> GET_ENTITY_BY_ID_METHOD;

    static {
        final NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();
        try {

            Class<?> minecraftServerType = CLASS_RESOLVER.resolve("server.MinecraftServer", "MinecraftServer");
            Object minecraftServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            MINECRAFT_SERVER = new ObjectWrapper(minecraftServer, minecraftServerType);

            Class<?> worldType = CLASS_RESOLVER.resolve("world.level.World", "World");
            MethodResolver methodResolver = new MethodResolver(worldType);
            GET_ENTITY_BY_ID_METHOD = methodResolver.resolveWrapper(
                    new ResolverQuery("getEntity", int.class),
                    new ResolverQuery("a", int.class)
            );
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public Entity getEntity(UUID uuid) {
        try {
            return MinecraftReflection.getBukkitEntity(MINECRAFT_SERVER.invoke("a", uuid));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Entity getEntity(World world, int id) {
        try {
            Object worldHandle = MinecraftReflection.getHandle(world);
            return MinecraftReflection.getBukkitEntity(GET_ENTITY_BY_ID_METHOD.invoke(worldHandle, id));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AbstractMovementImplementation movement(MovementListener movementListener) {
        return new BukkitMovementImplementation(movementListener);
    }
}
