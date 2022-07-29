package io.fairyproject.container.object.lifecycle.impl;

import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.lifecycle.ILifeCycle;
import io.fairyproject.container.object.lifecycle.LifeCycleHandler;
import io.fairyproject.util.ConditionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LifeCycleHandlerInterface implements LifeCycleHandler {

    private final ContainerObj containerObj;

    public LifeCycleHandlerInterface(ContainerObj containerObj) {
        this.containerObj = containerObj;

        ConditionUtils.is(ILifeCycle.class.isAssignableFrom(containerObj.type()), String.format("The container object must be implemented by %s", ILifeCycle.class));
    }

    @Override
    public CompletableFuture<?> onPreInit() {
        ILifeCycle instance = this.instance();
        return this.containerObj.threadingMode().execute(instance::onPreInit);
    }

    @Override
    public CompletableFuture<?> onPostInit() {
        ILifeCycle instance = this.instance();
        return this.containerObj.threadingMode().execute(instance::onPostInit);
    }

    @Override
    public CompletableFuture<?> onPreDestroy() {
        ILifeCycle instance = this.instance();
        return this.containerObj.threadingMode().execute(instance::onPreDestroy);
    }

    @Override
    public CompletableFuture<?> onPostDestroy() {
        ILifeCycle instance = this.instance();
        return this.containerObj.threadingMode().execute(instance::onPostDestroy);
    }

    @NotNull
    private ILifeCycle instance() {
        return ILifeCycle.class.cast(Objects.nonNull(this.containerObj.instance()));
    }
}
