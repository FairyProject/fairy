package io.fairyproject.container.object;

import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.container.Threading;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.container.object.lifecycle.LifeCycleHandler;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ContainerObj extends Terminable, TerminableConsumer {

    static ContainerObj of(Class<?> aClass) {
        return new ContainerObjImpl(aClass);
    }

    static ContainerObj of(Class<?> aClass, Object instance) {
        ContainerObj object = of(aClass);
        object.setInstance(instance);

        return object;
    }

    @NotNull Class<?> type();

    @Nullable Object instance();

    @NotNull Threading.Mode threadingMode();

    @Contract("_ -> this")
    @NotNull ContainerObj setThreadingMode(@NotNull Threading.Mode threadingMode);

    @NotNull LifeCycle lifeCycle();

    @NotNull CompletableFuture<?> setLifeCycle(@NotNull LifeCycle lifeCycle);

    @NotNull CompletableFuture<?> initLifeCycleHandlers();

    @Contract("_ -> this")
    @NotNull ContainerObj addLifeCycleHandler(@NotNull LifeCycleHandler lifeCycleHandler);

    void setInstance(@NotNull Object instance);

    @NotNull Collection<Class<?>> depends();

    @NotNull Collection<ContainerObj.DependEntry> dependEntries();

    void addDepend(@NotNull Class<?> dependClass, @NotNull ServiceDependencyType dependType);

    @NotNull Collection<ContainerObjCollector> collectors();

    @NotNull MetadataMap metadata();

    @ApiStatus.Internal
    void addCollector(@NotNull ContainerObjCollector collector);

    @ApiStatus.Internal
    void removeCollector(@NotNull ContainerObjCollector collector);

    @RequiredArgsConstructor
    @Data
    class DependEntry {

        public static DependEntry of(Class<?> dependClass, ServiceDependencyType dependType) {
            return new DependEntry(dependClass, dependType);
        }

        private final Class<?> dependClass;
        private final ServiceDependencyType dependType;

    }

}
