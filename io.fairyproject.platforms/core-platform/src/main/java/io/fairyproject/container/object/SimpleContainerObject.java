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

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.util.terminable.composite.CompositeClosingException;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public class SimpleContainerObject implements ContainerObject {

    private Object instance;
    private Class<?> type;

    private Plugin plugin;
    private Set<Class<?>> children;

    public SimpleContainerObject(Object instance, Class<?> type) {
        this.instance = instance;
        this.type = type;
        this.children = new HashSet<>();
    }

    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    @Override
    public void onDisable() {
        CompositeClosingException ex = this.compositeTerminable.closeSilently();
        if (ex != null) {
            ex.printAllStackTraces();
        }
    }

    @Override
    public boolean shouldInitialize() {
        return true;
    }

    @Override
    public CompletableFuture<?> lifeCycle(LifeCycle lifeCycle) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isLifeCycle(LifeCycle lifeCycle) {
        return true;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public @Nullable String getTag(String key) {
        return null;
    }

    @Override
    public boolean hasTag(String key) {
        return false;
    }

    @Override
    public void addTag(String key, String value) {

    }

    @Override
    public void setLifeCycle(LifeCycle lifeCycle) {

    }

    @Override
    public void setTags(Map<String, String> tags) {

    }

    @Override
    public LifeCycle getLifeCycle() {
        return null;
    }

    @Override
    public Map<String, String> getTags() {
        return null;
    }

    @Override
    public void bindWith(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin getBindPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isBind() {
        return this.plugin != null;
    }

    @Override
    public boolean hasDependencies() {
        return false;
    }

    public Set<Class<?>> getChildren() {
        return Collections.unmodifiableSet(this.children);
    }

    @Override
    public void addChildren(Class<?> children) {
        this.children.add(children);
    }

    @Override
    public void removeChildren(Class<?> children) {
        this.children.remove(children);
    }

    @Override
    public List<Class<?>> getDependencies(ServiceDependencyType type) {
        return Collections.emptyList();
    }

    @Override
    public Set<Map.Entry<ServiceDependencyType, List<Class<?>>>> getDependencyEntries() {
        return Collections.emptySet();
    }

    @Override
    public boolean isClosed() {
        return this.plugin.isClosed();
    }

    private boolean closed;

    @Override
    public void close() throws Exception {
        if (this.isClosed()) {
            return;
        }
        this.closed = true;

        this.onDisable();
        ContainerContext.INSTANCE.unregisterObject(this);
    }
}
