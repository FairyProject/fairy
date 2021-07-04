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

package org.fairy.bukkit.reflection.wrapper.impl;

import com.google.common.collect.Multimap;
import org.fairy.bukkit.reflection.resolver.FieldResolver;
import org.fairy.bukkit.reflection.wrapper.FieldWrapper;
import org.fairy.bukkit.reflection.wrapper.GuavaWrappers;
import org.fairy.bukkit.reflection.wrapper.SignedPropertyWrapper;
import org.fairy.util.collection.ConvertedMultimap;

import java.util.UUID;

public abstract class GameProfileImplementation {

    public static GameProfileImplementation getImplementation() {
        try {
            Class.forName("com.mojang.authlib.GameProfile");

            return new v1_8();
        } catch (ClassNotFoundException ignored) {
            return new v1_7();
        }
    }

    public abstract Class<?> getGameProfileClass();

    public abstract Object create(String name, UUID uuid);

    public abstract UUID getUuid(Object handle);

    public abstract String getName(Object handle);

    public abstract void setUuid(Object handle, UUID uuid);

    public abstract void setName(Object handle, String name);

    public abstract Multimap<String, SignedPropertyWrapper> getProperties(Object handle);

    public static class v1_8 extends GameProfileImplementation {

        private final FieldWrapper<UUID> uuidField;
        private final FieldWrapper<String> nameField;

        public v1_8() {
            try {
                FieldResolver fieldResolver = new FieldResolver(com.mojang.authlib.GameProfile.class);
                this.uuidField = fieldResolver.resolveByFirstTypeDynamic(UUID.class);
                this.nameField = fieldResolver.resolveByFirstTypeDynamic(String.class);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public Class<?> getGameProfileClass() {
            return com.mojang.authlib.GameProfile.class;
        }

        @Override
        public Object create(String name, UUID uuid) {
            return new com.mojang.authlib.GameProfile(uuid, name);
        }

        @Override
        public UUID getUuid(Object handle) {
            return ((com.mojang.authlib.GameProfile) handle).getId();
        }

        @Override
        public String getName(Object handle) {
            return ((com.mojang.authlib.GameProfile) handle).getName();
        }

        @Override
        public void setUuid(Object handle, UUID uuid) {
            this.uuidField.set(handle, uuid);
        }

        @Override
        public void setName(Object handle, String name) {
            this.nameField.set(handle, name);
        }

        @Override
        public Multimap<String, SignedPropertyWrapper> getProperties(Object handle) {
            com.mojang.authlib.GameProfile gameProfile = (com.mojang.authlib.GameProfile) handle;

            return new ConvertedMultimap<String, Object, SignedPropertyWrapper>(GuavaWrappers.warpMultimap(gameProfile.getProperties())) {

                @Override
                protected SignedPropertyWrapper toOuter(Object property) {
                    return new SignedPropertyWrapper(property);
                }

                @Override
                protected Object toInnerObject(Object outer) {
                    if (outer instanceof SignedPropertyWrapper) {
                        return toInner((SignedPropertyWrapper) outer);
                    }
                    return outer;
                }

                @Override
                protected Object toInner(SignedPropertyWrapper signedPropertyWrapper) {
                    return signedPropertyWrapper.getHandle();
                }

            };
        }
    }

    public static class v1_7 extends GameProfileImplementation {

        private final FieldWrapper<UUID> uuidField;
        private final FieldWrapper<String> nameField;

        public v1_7() {
            try {
                FieldResolver fieldResolver = new FieldResolver(net.minecraft.util.com.mojang.authlib.GameProfile.class);
                this.uuidField = fieldResolver.resolveByFirstTypeDynamic(UUID.class);
                this.nameField = fieldResolver.resolveByFirstTypeDynamic(String.class);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public Class<?> getGameProfileClass() {
            return net.minecraft.util.com.mojang.authlib.GameProfile.class;
        }

        @Override
        public Object create(String name, UUID uuid) {
            return new net.minecraft.util.com.mojang.authlib.GameProfile(uuid, name);
        }

        @Override
        public UUID getUuid(Object handle) {
            return ((net.minecraft.util.com.mojang.authlib.GameProfile) handle).getId();
        }

        @Override
        public String getName(Object handle) {
            return ((net.minecraft.util.com.mojang.authlib.GameProfile) handle).getName();
        }

        @Override
        public void setUuid(Object handle, UUID uuid) {
            this.uuidField.set(handle, uuid);
        }

        @Override
        public void setName(Object handle, String name) {
            this.nameField.set(handle, name);
        }

        @Override
        public Multimap<String, SignedPropertyWrapper> getProperties(Object handle) {
            net.minecraft.util.com.mojang.authlib.GameProfile gameProfile = (net.minecraft.util.com.mojang.authlib.GameProfile) handle;

            return new ConvertedMultimap<String, Object, SignedPropertyWrapper>(GuavaWrappers.warpMultimap(gameProfile.getProperties())) {

                @Override
                protected SignedPropertyWrapper toOuter(Object property) {
                    return new SignedPropertyWrapper(property);
                }

                @Override
                protected Object toInnerObject(Object outer) {
                    if (outer instanceof SignedPropertyWrapper) {
                        return toInner((SignedPropertyWrapper) outer);
                    }
                    return outer;
                }

                @Override
                protected Object toInner(SignedPropertyWrapper signedPropertyWrapper) {
                    return signedPropertyWrapper.getHandle();
                }

            };
        }
    }

}
