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

package io.fairyproject.container.binder;

import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.type.TypeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContainerObjectBinder {
    @Nullable ContainerObj getBinding(Class<?> type);

    @Nullable ContainerObj getExactBinding(Class<?> classType);

    boolean isBound(Class<?> type);

    void bind(Class<?> type, ContainerObj object);

    void unbind(Class<?> type);
    
    /**
     * Get a binding using type descriptor information, supporting generic types.
     *
     * @param typeDescriptor The type descriptor
     * @return The container object, or null if not found
     */
    @Nullable ContainerObj getBinding(@NotNull TypeDescriptor typeDescriptor);
    
    /**
     * Check if a binding exists for the given type descriptor.
     *
     * @param typeDescriptor The type descriptor
     * @return true if a binding exists
     */
    boolean isBound(@NotNull TypeDescriptor typeDescriptor);
    
    /**
     * Bind an object with its type descriptor.
     *
     * @param typeDescriptor The type descriptor
     * @param object The container object
     */
    void bind(@NotNull TypeDescriptor typeDescriptor, @NotNull ContainerObj object);
}
