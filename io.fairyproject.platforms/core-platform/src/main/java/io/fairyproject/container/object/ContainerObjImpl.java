package io.fairyproject.container.object;

import io.fairyproject.container.Threading;
import io.fairyproject.container.object.provider.InstanceProvider;
import io.fairyproject.container.scope.InjectableScope;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.util.AsyncUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ContainerObjImpl implements ContainerObj {

    private final Class<?> type;
    private final MetadataMap metadataMap;
    private final Set<Class<?>> dependencies;
    private Threading.Mode threadingMode;
    private InjectableScope scope;
    private InstanceProvider instanceProvider;

    public ContainerObjImpl(Class<?> type) {
        this.type = type;
        this.metadataMap = MetadataMap.create();
        this.dependencies = new HashSet<>();
        this.threadingMode = Threading.Mode.SYNC;
        this.scope = InjectableScope.SINGLETON;
    }

    @Override
    public @NotNull Class<?> getType() {
        return this.type;
    }

    @NotNull
    @Override
    public Threading.Mode getThreadingMode() {
        return this.threadingMode;
    }

    @Override
    public void setThreadingMode(Threading.@NotNull Mode threadingMode) {
        this.threadingMode = threadingMode;
    }

    @Override
    public @NotNull InjectableScope getScope() {
        return this.scope;
    }

    @Override
    public void setScope(@NotNull InjectableScope scope) {
        this.scope = scope;
    }

    @Override
    public InstanceProvider getInstanceProvider() {
        return this.instanceProvider;
    }

    @Override
    public void setInstanceProvider(InstanceProvider instanceProvider) {
        this.instanceProvider = instanceProvider;
    }

    @Override
    public @NotNull Collection<Class<?>> getDependencies() {
        return Collections.unmodifiableCollection(this.dependencies);
    }

    @Override
    public void addDependency(@NotNull Class<?> dependClass) {
        this.dependencies.add(dependClass);
    }

    @Override
    public @NotNull MetadataMap getMetadata() {
        return this.metadataMap;
    }

    @Override
    public CompletableFuture<Object> createInstanceFromProvider(Object[] objects) {
        InstanceProvider instanceProvider = this.getInstanceProvider();
        if (instanceProvider == null)
            return AsyncUtils.empty();

        return this.getThreadingMode().execute(() -> {
            Object instance;
            try {
                instance = instanceProvider.provide(objects);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to provide instance for " + this.getType().getName(), e);
            }

            return instance;
        });
    }

    @Override
    public String toString() {
        return this.getType().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ContainerObjImpl that = (ContainerObjImpl) o;
        if (this.getType() != that.getType())
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type);
    }

}
