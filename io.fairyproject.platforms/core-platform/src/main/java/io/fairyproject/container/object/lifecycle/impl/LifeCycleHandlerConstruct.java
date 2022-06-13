package io.fairyproject.container.object.lifecycle.impl;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.lifecycle.LifeCycleHandler;
import io.fairyproject.container.object.resolver.ConstructorContainerResolver;
import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.exceptionally.ThrowingRunnable;

import java.util.concurrent.CompletableFuture;

public class LifeCycleHandlerConstruct implements LifeCycleHandler {

    private final ContainerObj obj;
    private ConstructorContainerResolver constructor;

    public LifeCycleHandlerConstruct(ContainerObj obj) {
        this.obj = obj;
    }

    @Override
    public void init() {
        if (this.obj.instance() != null)
            return;
        this.constructor = new ConstructorContainerResolver(obj.type());
        for (Class<?> type : this.constructor.getTypes()) {
            this.obj.addDepend(type, ServiceDependencyType.FORCE);
        }
    }

    @Override
    public CompletableFuture<?> onConstruct() {
        if (this.obj.instance() != null)
            return AsyncUtils.empty();

        return this.obj.threadingMode().execute(ThrowingRunnable.sneaky(() -> {
            Object instance = this.constructor.newInstance(ContainerContext.get());
            this.obj.setInstance(instance);
        }));
    }
}
