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
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerObjectBinderImpl implements ContainerObjectBinder {

    private final Map<Class<?>, ContainerObj> bindings = new ConcurrentHashMap<>();

    @Override
    @Nullable
    public ContainerObj getBinding(Class<?> type) {
        ContainerObj obj = this.bindings.get(type);
        if (obj == null) {
            obj = findBindingAssignableByType(type);
        }
        return obj;
    }

    @Override
    public @Nullable ContainerObj getExactBinding(Class<?> classType) {
        return this.bindings.get(classType);
    }

    private ContainerObj findBindingAssignableByType(Class<?> type) {
        for (ContainerObj value : this.bindings.values()) {
            Class<?> valueType = value.getType();
            if (type.isAssignableFrom(valueType)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public boolean isBound(Class<?> type) {
        return this.getBinding(type) != null;
    }

    @Override
    public void bind(Class<?> type, ContainerObj object) {
        this.bindings.put(type, object);
    }

    @Override
    public void unbind(Class<?> type) {
        this.bindings.remove(type);
    }

}
