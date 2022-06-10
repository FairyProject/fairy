package io.fairyproject.container.object;

import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.container.Threading;
import io.fairyproject.container.object.lifecycle.LifeCycleChangeHandler;
import io.fairyproject.util.AsyncUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContainerObjImpl implements ContainerObj {

    private final Set<ContainerObj.DependEntry> depends = ConcurrentHashMap.newKeySet();
    private final Class<?> type;

    private LifeCycleChangeHandler lifeCycleChangeHandler;
    private Object instance = null;
    private LifeCycle lifeCycle = LifeCycle.NONE;

    private Threading.Mode threadingMode = Threading.Mode.SYNC;

    public ContainerObjImpl(Class<?> type) {
        this.type = type;
    }

    @Override
    public @NotNull Class<?> type() {
        return this.type;
    }

    @Override
    public @Nullable Object instance() {
        return this.instance;
    }

    @Override
    public @NotNull LifeCycle lifeCycle() {
        return this.lifeCycle;
    }

    @Override
    public @NotNull Threading.Mode threadingMode() {
        return this.threadingMode;
    }

    @Override
    public @NotNull ContainerObj setThreadingMode(Threading.@NotNull Mode threadingMode) {
        this.threadingMode = threadingMode;
        return this;
    }

    @Override
    public @NotNull CompletableFuture<?> setLifeCycle(@NotNull LifeCycle lifeCycle) {
        if (lifeCycle == this.lifeCycle) {
            return AsyncUtils.empty();
        }
        this.lifeCycle = lifeCycle;
        if (this.lifeCycleChangeHandler != null)
            return this.lifeCycleChangeHandler.apply(lifeCycle);
        else
            return AsyncUtils.empty();
    }

    @Override
    public @NotNull ContainerObj lifeCycleChangeHandler(@NotNull LifeCycleChangeHandler lifeCycleChangeHandler) {
        this.lifeCycleChangeHandler = lifeCycleChangeHandler;
        return this;
    }

    @Override
    public void setInstance(@NotNull Object instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull Collection<Class<?>> depends() {
        return this.depends.stream()
                .map(ContainerObj.DependEntry::getDependClass)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Collection<ContainerObj.DependEntry> dependEntries() {
        return Collections.unmodifiableSet(this.depends);
    }

    @Override
    public void addDepend(@NotNull Class<?> dependClass, @NotNull ServiceDependencyType dependType) {
        this.depends.add(ContainerObj.DependEntry.of(dependClass, dependType));
    }
}
