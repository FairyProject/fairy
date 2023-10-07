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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonObjectRegistryImpl implements SingletonObjectRegistry {

    private final Map<Class<?>, Object> objectByType = new ConcurrentHashMap<>();
    private final Map<Class<?>, LifeCycle> lifeCycleByType = new ConcurrentHashMap<>();

    @Override
    public void registerSingleton(Class<?> type, Object instance) {
        synchronized (this.objectByType) {
            Object previous = this.objectByType.get(type);
            if (previous != null) {
                throw new IllegalStateException("Could not register object [" + instance + "] under type [" + type.getName() + "]: there is already object [" + previous + "] bound");
            }

            this.objectByType.put(type, instance);
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
    }
}
