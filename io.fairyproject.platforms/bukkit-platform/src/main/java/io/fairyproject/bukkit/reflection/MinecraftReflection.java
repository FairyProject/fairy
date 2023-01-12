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

package io.fairyproject.bukkit.reflection;

import io.fairyproject.Debug;
import io.fairyproject.bukkit.reflection.minecraft.OBCVersion;
import io.fairyproject.bukkit.reflection.resolver.ConstructorResolver;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.EquivalentConverter;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Helper class to access minecraft/bukkit specific objects
 *
 * @deprecated
 */
@Deprecated
public class MinecraftReflection {

    public static String NETTY_PREFIX;

    private static Class<?> NMS_ENTITY;
    private static Class<?> CRAFT_ENTITY;

    /**
     * Netty Channel Type
     */
    public static Class<?> CHANNEL_TYPE;

    /**
     * GameProfile Type
     */
    public static Class<?> GAME_PROFILE_TYPE;

    public static void init(BukkitNMSManager nmsManager) {
        if (Debug.UNIT_TEST) {
            return;
        }

        try {
            NMS_ENTITY = nmsManager.getNmsClassResolver().resolve("world.entity.Entity", "Entity");
            CRAFT_ENTITY = nmsManager.getObcClassResolver().resolve("entity.CraftEntity");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        try {
            CHANNEL_TYPE = Class.forName("io.netty.channel.Channel");
            NETTY_PREFIX = "io.netty.";
        } catch (ClassNotFoundException ex) {

            try {
                CHANNEL_TYPE = Class.forName("net.minecraft.util.io.netty.channel.Channel");
                NETTY_PREFIX = "net.minecraft.util.io.netty.";
            } catch (ClassNotFoundException ex2) {
                throw new IllegalStateException("Coulnd't find netty Channel class!", ex2);
            }

        }

        try {
            GAME_PROFILE_TYPE = Class.forName("com.mojang.authlib.GameProfile");
        } catch (ClassNotFoundException ex) {
            try {
                GAME_PROFILE_TYPE = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
            } catch (ClassNotFoundException ex2) {
                throw new IllegalStateException("Coulnd't find mojang GameProfile class!", ex2);
            }
        }
    }

    /**
     * @return the current NMS/OBC version (format <code>&lt;version&gt;.</code>
     */
    public static String getVersion() {
        return OBCVersion.get().packageName() + ".";
    }

    /**
     * @return the current NMS version package
     */
    public static String getNMSPackage() {
        return OBCVersion.get().getNmsPackage();
    }

    /**
     * @return the current OBC package
     */
    public static String getOBCPackage() {
        return OBCVersion.get().getObcPackage();
    }

    public static Object getHandle(Object object) throws ReflectiveOperationException {
        Method method;
        try {
            method = AccessUtil.setAccessible(object.getClass().getDeclaredMethod("getHandle"));
        } catch (ReflectiveOperationException e) {
            method = AccessUtil.setAccessible(CRAFT_ENTITY.getDeclaredMethod("getHandle"));
        }
        return method.invoke(object);
    }

    public static Entity getBukkitEntity(Object object) throws ReflectiveOperationException {
        Method method;
        try {
            method = AccessUtil.setAccessible(NMS_ENTITY.getDeclaredMethod("getBukkitEntity"));
        } catch (ReflectiveOperationException e) {
            method = AccessUtil.setAccessible(CRAFT_ENTITY.getDeclaredMethod("getHandle"));
        }
        return (Entity) method.invoke(object);
    }

    public static Object getHandleSilent(Object object) {
        try {
            return getHandle(object);
        } catch (Exception e) {
        }
        return null;
    }

    @Deprecated
    public static Object newEnumInstance(Class clazz, Class[] types, Object[] values) throws ReflectiveOperationException {
        Constructor constructor = new ConstructorResolver(clazz).resolve(types);
        Field accessorField = new FieldResolver(Constructor.class).resolve("constructorAccessor");
        Object constructorAccessor = accessorField.get(constructor);
        if (constructorAccessor == null) {
            new MethodResolver(Constructor.class).resolve("acquireConstructorAccessor").invoke(constructor);
            constructorAccessor = accessorField.get(constructor);
        }
        return new MethodResolver(constructorAccessor.getClass()).resolve("newInstance").invoke(constructorAccessor, (Object) values);
    }


    public static <T> EquivalentConverter<T> handle(final Function<T, Object> toHandle,
                                                    final Function<Object, T> fromHandle) {
        return new EquivalentConverter<T>() {
            @Override
            public T getSpecific(Object generic) {
                return fromHandle.apply(generic);
            }

            @Override
            public Object getGeneric(T specific) {
                return toHandle.apply(specific);
            }

            @Override
            public Class<T> getSpecificType() {
                return null;
            }
        };
    }
}
