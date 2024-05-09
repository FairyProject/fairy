package io.fairyproject.container.object;

import io.fairyproject.container.Threading;
import io.fairyproject.container.object.provider.InstanceProvider;
import io.fairyproject.container.scope.InjectableScope;
import io.fairyproject.metadata.MetadataMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ContainerObj {

    static ContainerObj create(Class<?> objClass) {
        return new ContainerObjImpl(objClass);
    }

    @NotNull Class<?> getType();

    @NotNull Threading.Mode getThreadingMode();

    void setThreadingMode(@NotNull Threading.Mode threadingMode);

    @NotNull InjectableScope getScope();

    void setScope(@NotNull InjectableScope scope);

    InstanceProvider getInstanceProvider();

    void setInstanceProvider(InstanceProvider instanceProvider);

    @NotNull Collection<Class<?>> getDependencies();

    void addDependency(@NotNull Class<?> dependClass);

    @NotNull MetadataMap getMetadata();

    default boolean isSingletonScope() {
        return this.getScope() == InjectableScope.SINGLETON;
    }

    default boolean isPrototypeScope() {
        return this.getScope() == InjectableScope.PROTOTYPE;
    }

    CompletableFuture<Object> createInstanceFromProvider(Object[] objects);

}
