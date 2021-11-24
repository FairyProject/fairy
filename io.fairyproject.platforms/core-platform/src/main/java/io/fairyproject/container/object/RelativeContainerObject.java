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

package io.fairyproject.container.object;

import com.google.common.collect.Lists;
import io.fairyproject.container.ServiceDependencyType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelativeContainerObject extends BaseContainerObject {

    protected final Map<ServiceDependencyType, List<Class<?>>> dependencies;

    public RelativeContainerObject(Object instance) {
        this(instance.getClass(), instance);
    }

    public RelativeContainerObject(Class<?> type) {
        super(type);
        this.dependencies = new HashMap<>();
        for (ServiceDependencyType dependencyType : ServiceDependencyType.values()) {
            this.dependencies.put(dependencyType, Lists.newArrayList());
        }
    }

    public RelativeContainerObject(Class<?> type, Class<?>[] dependencies) {
        this(type);
        this.addDependencies(ServiceDependencyType.FORCE, dependencies);
    }

    public RelativeContainerObject(Class<?> type, @Nullable Object instance) {
        super(type, instance);
        this.dependencies = new HashMap<>();
        for (ServiceDependencyType dependencyType : ServiceDependencyType.values()) {
            this.dependencies.put(dependencyType, Lists.newArrayList());
        }
    }

    public RelativeContainerObject(Class<?> type, @Nullable Object instance, Class<?>[] dependencies) {
        this(type, instance);
        this.addDependencies(ServiceDependencyType.FORCE, dependencies);
    }

    public void addDependencies(ServiceDependencyType type, Class<?>... dependencies) {
        for (Class<?> dependency : dependencies) {
            this.getDependencies(type).add(dependency);
        }
    }

    public List<Class<?>> getDependencies(ServiceDependencyType type) {
        return this.dependencies.get(type);
    }

    public Set<Map.Entry<ServiceDependencyType, List<Class<?>>>> getDependencyEntries() {
        return this.dependencies.entrySet();
    }

    @Override
    public boolean hasDependencies() {
        return this.dependencies.size() > 0;
    }
}
