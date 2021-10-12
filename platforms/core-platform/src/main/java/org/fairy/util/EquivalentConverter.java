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

package org.fairy.util;

import lombok.Getter;

import javax.annotation.Nullable;

/**
 * Interface that converts generic objects into types and back.
 *
 * @author Kristian
 * @param <T> The specific type.
 */
public interface EquivalentConverter<T> {
    /**
     * Retrieve a copy of the generic type from a specific type.
     * <p>
     * This is usually a native net.minecraft.server type in Minecraft.
     * @param specific - the specific type we need to copy.
     * @return A copy of the specific type.
     */
    Object getGeneric(T specific);

    /**
     * Retrieve a copy of the specific type using an instance of the generic type.
     * <p>
     * This is usually a wrapper type in the Bukkit API or ProtocolLib API.
     * @param generic - the generic type.
     * @return The new specific type.
     */
    T getSpecific(Object generic);

    /**
     * Due to type erasure, we need to explicitly keep a reference to the specific type.
     * @return The specific type.
     */
    Class<T> getSpecificType();

    @Getter
    public static class EnumConverter<T extends Enum<T>> implements EquivalentConverter<T> {
        private Class<? extends Enum> genericType;
        private Class<T> specificType;

        public EnumConverter(@Nullable Class<? extends Enum> genericType, @Nullable Class<T> specificType) {
            this.genericType = genericType;
            this.specificType = specificType;
        }

        @Override
        public T getSpecific(@Nullable Object generic) {
            if (generic == null) {
                return this.getDefaultSpecific();
            }
            return Enum.valueOf(specificType, ((Enum) generic).name());
        }

        @Override
        public Object getGeneric(@Nullable T specific) {
            if (specific == null) {
                return this.getDefaultGeneric();
            }
            return Enum.valueOf((Class) genericType, specific.name());
        }

        @Nullable
        public T getDefaultSpecific() {
            return null;
        }

        @Nullable
        public Object getDefaultGeneric() {
            return null;
        }

        void setGenericType(Class<? extends Enum> genericType) {
            this.genericType = genericType;
        }
    }
}