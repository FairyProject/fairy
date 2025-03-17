package io.fairyproject.container.object;

import io.fairyproject.container.Threading;
import io.fairyproject.container.object.provider.InstanceProvider;
import io.fairyproject.container.scope.InjectableScope;
import io.fairyproject.container.type.TypeDescriptor;
import io.fairyproject.data.MetaStorage;
import io.fairyproject.util.AsyncUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ContainerObjImpl implements ContainerObj {

    private final Class<?> type;
    private final MetaStorage metaStorage;
    private final Set<Class<?>> dependencies;
    private Threading.Mode threadingMode;
    private InjectableScope scope;
    private InstanceProvider instanceProvider;
    private TypeDescriptor typeDescriptor;

    public ContainerObjImpl(Class<?> type) {
        this.type = type;
        this.metaStorage = MetaStorage.create();
        this.dependencies = new HashSet<>();
        this.threadingMode = Threading.Mode.SYNC;
        this.scope = InjectableScope.SINGLETON;
        this.typeDescriptor = new TypeDescriptor(type);
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
        Set<Class<?>> dependencies = new HashSet<>(this.dependencies);
        if (this.instanceProvider != null)
            dependencies.addAll(Arrays.asList(this.instanceProvider.getDependencies()));
        return dependencies;
    }

    @Override
    public void addDependency(@NotNull Class<?> dependClass) {
        this.dependencies.add(dependClass);
    }

    @Override
    public @NotNull MetaStorage getMetadata() {
        return this.metaStorage;
    }

    @Override
    public @NotNull TypeDescriptor getTypeDescriptor() {
        return this.typeDescriptor;
    }

    @Override
    public void setTypeDescriptor(@NotNull TypeDescriptor typeDescriptor) {
        this.typeDescriptor = typeDescriptor;
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
        return this.typeDescriptor.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ContainerObjImpl that = (ContainerObjImpl) o;
        return Objects.equals(this.typeDescriptor, that.typeDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.typeDescriptor);
    }
}
