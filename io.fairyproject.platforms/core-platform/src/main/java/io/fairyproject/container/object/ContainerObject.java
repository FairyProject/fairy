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

import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface ContainerObject extends TerminableConsumer, Terminable {

    /**
     * Should this bean be initialized
     *
     * @return should be initialized
     * @throws InvocationTargetException reflection error
     * @throws IllegalAccessException reflection error
     */
    boolean shouldInitialize() throws InvocationTargetException, IllegalAccessException;

    void lifeCycle(LifeCycle lifeCycle);

    boolean isLifeCycle(LifeCycle lifeCycle);

    LifeCycle getLifeCycle();

    boolean isActivated();

    boolean isDestroyed();

    @Nullable
    String getTag(String key);

    boolean hasTag(String key);

    void addTag(String key, String value);

    void setLifeCycle(LifeCycle lifeCycle);

    void setInstance(Object instance);

    void setType(Class<?> type);

    void setTags(Map<String, String> tags);

    @Nullable
    Object getInstance();

    Class<?> getType();

    Map<String, String> getTags();

    void bindWith(Plugin plugin);

    Plugin getBindPlugin();

    boolean isBind();

    boolean hasDependencies();

    Set<Class<?>> getChildren();

    void addChildren(Class<?> children);

    void removeChildren(Class<?> children);

    List<Class<?>> getDependencies(ServiceDependencyType type);

    Set<Map.Entry<ServiceDependencyType, List<Class<?>>>> getDependencyEntries();

    default Set<Class<?>> getAllDependencies() {
        return this.getDependencyEntries().stream()
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    default void onEnable() {

    }

    default void onDisable() {

    }

}
