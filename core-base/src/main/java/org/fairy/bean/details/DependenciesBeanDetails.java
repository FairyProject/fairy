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

package org.fairy.bean.details;

import com.google.common.collect.Lists;
import org.fairy.bean.Service;
import org.fairy.bean.ServiceDependencyType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependenciesBeanDetails extends GenericBeanDetails {

    protected final Map<ServiceDependencyType, List<String>> dependencies;

    public DependenciesBeanDetails(Object instance) {
        this(instance.getClass(), instance, "dummy");
    }

    public DependenciesBeanDetails(Object instance, Service service) {
        this(instance.getClass(), instance, service.name());
    }

    public DependenciesBeanDetails(Class<?> type, String name) {
        super(type, name);
        this.dependencies = new HashMap<>();
        for (ServiceDependencyType dependencyType : ServiceDependencyType.values()) {
            this.dependencies.put(dependencyType, Lists.newArrayList());
        }
    }

    public DependenciesBeanDetails(Class<?> type, String name, String[] dependencies) {
        this(type, name);
        this.addDependencies(ServiceDependencyType.FORCE, dependencies);
    }

    public DependenciesBeanDetails(Class<?> type, @Nullable Object instance, String name) {
        super(type, instance, name);
        this.dependencies = new HashMap<>();
        for (ServiceDependencyType dependencyType : ServiceDependencyType.values()) {
            this.dependencies.put(dependencyType, Lists.newArrayList());
        }
    }

    public DependenciesBeanDetails(Class<?> type, @Nullable Object instance, String name, String[] dependencies) {
        this(type, instance, name);
        this.addDependencies(ServiceDependencyType.FORCE, dependencies);
    }

    public void addDependencies(ServiceDependencyType type, String... dependencies) {
        for (String dependency : dependencies) {
            this.getDependencies(type).add(dependency);
        }
    }

    public List<String> getDependencies(ServiceDependencyType type) {
        return this.dependencies.get(type);
    }

    public Set<Map.Entry<ServiceDependencyType, List<String>>> getDependencyEntries() {
        return this.dependencies.entrySet();
    }

    @Override
    public boolean hasDependencies() {
        return this.dependencies.size() > 0;
    }
}
