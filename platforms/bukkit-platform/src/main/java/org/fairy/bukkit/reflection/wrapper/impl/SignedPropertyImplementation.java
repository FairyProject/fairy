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

public abstract class SignedPropertyImplementation {

    public static SignedPropertyImplementation getImplementation() {
        try {
            Class.forName("com.mojang.authlib.GameProfile");

            return new v1_8();
        } catch (ClassNotFoundException ignored) {
            return new v1_7();
        }
    }

    public abstract Object create(String name, String value, String signature);

    public abstract String getName(Object handle);

    public abstract String getValue(Object handle);

    public abstract String getSignature(Object handle);

    public static class v1_8 extends SignedPropertyImplementation {

        @Override
        public Object create(String name, String value, String signature) {
            return new com.mojang.authlib.properties.Property(name, value, signature);
        }

        @Override
        public String getName(Object handle) {
            return ((com.mojang.authlib.properties.Property) handle).getName();
        }

        @Override
        public String getValue(Object handle) {
            return ((com.mojang.authlib.properties.Property) handle).getValue();
        }

        @Override
        public String getSignature(Object handle) {
            return ((com.mojang.authlib.properties.Property) handle).getSignature();
        }
    }

    public static class v1_7 extends SignedPropertyImplementation {

        @Override
        public Object create(String name, String value, String signature) {
            return new net.minecraft.util.com.mojang.authlib.properties.Property(name, value, signature);
        }

        @Override
        public String getName(Object handle) {
            return ((net.minecraft.util.com.mojang.authlib.properties.Property) handle).getName();
        }

        @Override
        public String getValue(Object handle) {
            return ((net.minecraft.util.com.mojang.authlib.properties.Property) handle).getValue();
        }

        @Override
        public String getSignature(Object handle) {
            return ((net.minecraft.util.com.mojang.authlib.properties.Property) handle).getSignature();
        }
    }

}
