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

package io.fairyproject.container.object.singleton;

import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.type.TypeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonObjectRegistryImpl implements SingletonObjectRegistry {

    private final Map<Class<?>, Object> objectByType = new ConcurrentHashMap<>();
    private final Map<Class<?>, LifeCycle> lifeCycleByType = new ConcurrentHashMap<>();

    private final Map<TypeDescriptor, Object> objectByTypeDescriptor = new ConcurrentHashMap<>();
    private final Map<TypeDescriptor, LifeCycle> lifeCycleByTypeDescriptor = new ConcurrentHashMap<>();

    @Override
    public void registerSingleton(Class<?> type, Object instance) {
        synchronized (this.objectByType) {
            Object previous = this.objectByType.get(type);
            if (previous != null) {
                throw new IllegalStateException("Could not register object [" + instance + "] under type ["
                        + type.getName() + "]: there is already object [" + previous + "] bound");
            }

            this.objectByType.put(type, instance);
            this.objectByTypeDescriptor.put(new TypeDescriptor(type), instance);
        }
    }

    @Override
    public Object getSingleton(Class<?> type) {
        return objectByType.get(type);
    }

    @Override
    public boolean containsSingleton(Class<?> type) {
        return objectByType.containsKey(type);
    }

    @Override
    public void removeSingleton(Class<?> type) {
        objectByType.remove(type);
        lifeCycleByType.remove(type);

        TypeDescriptor typeDescriptor = new TypeDescriptor(type);
        objectByTypeDescriptor.remove(typeDescriptor);
        lifeCycleByTypeDescriptor.remove(typeDescriptor);
    }

    @Override
    public Set<Class<?>> getSingletonTypes() {
        return Collections.unmodifiableSet(objectByType.keySet());
    }

    @Override
    public LifeCycle getSingletonLifeCycle(Class<?> type) {
        return lifeCycleByType.getOrDefault(type, LifeCycle.NONE);
    }

    @Override
    public void setSingletonLifeCycle(Class<?> type, LifeCycle lifeCycle) {
        lifeCycleByType.put(type, lifeCycle);

        TypeDescriptor typeDescriptor = new TypeDescriptor(type);
        lifeCycleByTypeDescriptor.put(typeDescriptor, lifeCycle);
    }

    @Override
    public void registerSingleton(@NotNull TypeDescriptor typeDescriptor, @NotNull Object instance) {
        synchronized (this.objectByTypeDescriptor) {
            Object previous = this.objectByTypeDescriptor.get(typeDescriptor);
            if (previous != null) {
                throw new IllegalStateException("Could not register object [" + instance + "] under type ["
                        + typeDescriptor + "]: there is already object [" + previous + "] bound");
            }

            this.objectByTypeDescriptor.put(typeDescriptor, instance);
            this.objectByType.put(typeDescriptor.getRawType(), instance);
        }
    }

    @Override
    @Nullable
    public Object getSingleton(@NotNull TypeDescriptor typeDescriptor) {
        Object obj = objectByTypeDescriptor.get(typeDescriptor);
        if (obj != null) {
            return obj;
        }

        for (Map.Entry<TypeDescriptor, Object> entry : objectByTypeDescriptor.entrySet()) {
            if (entry.getKey().isAssignableTo(typeDescriptor)) {
                return entry.getValue();
            }
        }

        return objectByType.get(typeDescriptor.getRawType());
    }

    @Override
    public boolean containsSingleton(@NotNull TypeDescriptor typeDescriptor) {
        return getSingleton(typeDescriptor) != null;
    }

    @Override
    public void removeSingleton(@NotNull TypeDescriptor typeDescriptor) {
        objectByTypeDescriptor.remove(typeDescriptor);
        lifeCycleByTypeDescriptor.remove(typeDescriptor);
        Type[] genericTypes = typeDescriptor.getGenericTypes();

        if (genericTypes == null || genericTypes.length == 0) {
            Class<?> rawType = typeDescriptor.getRawType();

            objectByType.remove(rawType);
            lifeCycleByType.remove(rawType);
        }
    }

    @Override
    @NotNull
    public Set<TypeDescriptor> getSingletonTypeDescriptors() {
        return Collections.unmodifiableSet(objectByTypeDescriptor.keySet());
    }

    @Override
    @NotNull
    public LifeCycle getSingletonLifeCycle(@NotNull TypeDescriptor typeDescriptor) {
        LifeCycle lifeCycle = lifeCycleByTypeDescriptor.get(typeDescriptor);
        if (lifeCycle != null) {
            return lifeCycle;
        }

        for (Map.Entry<TypeDescriptor, LifeCycle> entry : lifeCycleByTypeDescriptor.entrySet()) {
            if (entry.getKey().isAssignableTo(typeDescriptor)) {
                return entry.getValue();
            }
        }

        return lifeCycleByType.getOrDefault(typeDescriptor.getRawType(), LifeCycle.NONE);
    }

    @Override
    public void setSingletonLifeCycle(@NotNull TypeDescriptor typeDescriptor, @NotNull LifeCycle lifeCycle) {
        lifeCycleByTypeDescriptor.put(typeDescriptor, lifeCycle);
        Type[] genericTypes = typeDescriptor.getGenericTypes();

        if (genericTypes == null || genericTypes.length == 0) {
            lifeCycleByType.put(typeDescriptor.getRawType(), lifeCycle);
        }
    }

}
