/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.container.node.loader;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.ContainerLogger;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.loader.collection.InstanceCollection;
import io.fairyproject.container.node.loader.collection.InstanceEntry;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.provider.InstanceProvider;
import io.fairyproject.container.object.resolver.ContainerObjectResolver;
import io.fairyproject.container.object.singleton.SingletonObjectRegistry;
import io.fairyproject.container.processor.ContainerNodeInitProcessor;
import io.fairyproject.container.processor.ContainerObjConstructProcessor;
import io.fairyproject.container.processor.ContainerObjInitProcessor;
import io.fairyproject.container.scope.InjectableScope;
import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.thread.BlockingThreadAwaitQueue;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ContainerNodeLoader {

    private final ContainerContext context;
    private final ContainerNode node;

    private ContainerObjectResolver containerObjectResolver;
    private InstanceCollection collection;

    public boolean load() {
        this.containerObjectResolver = ContainerObjectResolver.create(
                this.context.containerObjectBinder(),
                this::findSingletonInstance,
                this::findPrototypeInstance
        );
        this.collection = InstanceCollection.create();

        BlockingThreadAwaitQueue queue = BlockingThreadAwaitQueue.create();

        this.node.resolve();
        if (!this.node.isResolved())
            return false;

        CompletableFuture<?> completableFuture = this.provideInstances()
                .thenRun(this::callNodePreInitProcessors)
                .thenComposeAsync(directlyCompose(this::callPreInitProcessors), queue)
                .thenRun(this::handleObjCollector)
                .thenComposeAsync(directlyCompose(this::callPostInitProcessors), queue)
                .thenRun(this::callNodePostInitProcessors);

        queue.await(completableFuture::isDone);
        ThrowingRunnable.sneaky(completableFuture::get).run();
        return true;
    }

    private void callNodePreInitProcessors() {
        for (ContainerNodeInitProcessor nodeInitProcessor : context.nodeInitProcessors()) {
            nodeInitProcessor.processNodePreInitialization(this.node, this.containerObjectResolver);
        }
    }

    private void callNodePostInitProcessors() {
        for (ContainerNodeInitProcessor nodeInitProcessor : context.nodeInitProcessors()) {
            nodeInitProcessor.processNodePostInitialization(this.node, this.containerObjectResolver);
        }
    }

    private CompletableFuture<Object> findSingletonInstance(Class<?> type) {
        SingletonObjectRegistry singletonObjectRegistry = this.context.singletonObjectRegistry();
        Object instance = singletonObjectRegistry.getSingleton(type);
        if (instance == null)
            throw new IllegalStateException("Singleton instance for " + type.getName() + " is null!");

        return CompletableFuture.completedFuture(instance);
    }

    private CompletableFuture<Object> findPrototypeInstance(Class<?> type) {
        ContainerObj obj = this.context.containerObjectBinder().getBinding(type);
        if (obj == null)
            throw new IllegalStateException("Container object for " + type.getName() + " is null!");

        try {
            return this.provideInstance(obj);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to provide instance for " + type.getName(), e);
        }
    }

    private CompletableFuture<?> callPreInitProcessors() {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (InstanceEntry entry : this.collection) {
            Object instance = entry.getInstance();
            ContainerObj object = entry.getContainerObject();
            if (!this.trySetLifeCycle(object, LifeCycle.PRE_INIT))
                continue;

            CompletableFuture<?> chain = null;
            for (ContainerObjInitProcessor initProcessor : this.context.initProcessors()) {
                Supplier<CompletableFuture<?>> callback = () -> initProcessor.processPreInitialization(object, instance, this.containerObjectResolver);
                if (chain == null)
                    chain = callback.get();
                else
                    chain = chain.thenCompose($ -> callback.get());
            }

            if (chain != null)
                futures.add(chain);
        }

        return AsyncUtils.allOf(futures);
    }

    private CompletableFuture<?> callPostInitProcessors() {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (InstanceEntry entry : this.collection) {
            Object instance = entry.getInstance();
            ContainerObj object = entry.getContainerObject();
            if (!this.trySetLifeCycle(object, LifeCycle.POST_INIT))
                continue;

            CompletableFuture<?> chain = null;
            for (ContainerObjInitProcessor initProcessor : this.context.initProcessors()) {
                Supplier<CompletableFuture<?>> callback = () -> initProcessor.processPostInitialization(object, instance);
                if (chain == null)
                    chain = callback.get();
                else
                    chain = chain.thenCompose($ -> callback.get());
            }

            if (chain != null)
                futures.add(chain);
        }

        return AsyncUtils.allOf(futures);
    }

    private CompletableFuture<?> provideInstances() {
        return this.node.forEachClockwiseAwait(obj -> {
            if (obj.isPrototypeScope())
                return AsyncUtils.empty();

            try {
                return this.provideInstance(obj);
            } catch (Throwable throwable) {
                ContainerLogger.report(this.node, obj, throwable, "providing instance");
                return AsyncUtils.failureOf(throwable);
            }
        });
    }

    private CompletableFuture<Object> provideInstance(ContainerObj obj) throws Exception {
        Class<?> objectType = obj.getType();
        SingletonObjectRegistry singletonObjectRegistry = this.context.singletonObjectRegistry();

        if (obj.isSingletonScope()) {
            if (!this.trySetLifeCycle(obj, LifeCycle.CONSTRUCT)) {
                return AsyncUtils.empty();
            }

            if (singletonObjectRegistry.containsSingleton(objectType)) {
                Object instance = singletonObjectRegistry.getSingleton(objectType);
                this.postInstanceConstruct(instance, obj);

                return AsyncUtils.empty();
            }
        }

        InstanceProvider instanceProvider = obj.getInstanceProvider();
        if (instanceProvider == null) {
            throw new IllegalStateException("Instance provider for " + objectType.getName() + " is null!");
        }

        CompletableFuture<Object[]> future = this.containerObjectResolver.resolveInstances(instanceProvider.getDependencies());
        return future
                .thenApplyAsync(objects -> createInstance(obj, objects, instanceProvider), obj.getThreadingMode().getExecutor())
                .thenCompose(this::callConstructProcessors)
                .thenApply(instance -> {
                    if (obj.isSingletonScope()) {
                        singletonObjectRegistry.registerSingleton(objectType, instance);
                    }

                    this.postInstanceConstruct(instance, obj);
                    return instance;
                });
    }

    private void postInstanceConstruct(Object instance, ContainerObj obj) {
        this.collection.add(instance, obj);
    }

    @NotNull
    private static Object createInstance(ContainerObj obj, Object[] dependencies, InstanceProvider instanceProvider) {
        Object instance;
        try {
            instance = instanceProvider.provide(dependencies);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to provide instance for " + obj.getType().getName(), ex);
        }

        return instance;
    }

    private CompletableFuture<Object> callConstructProcessors(Object instance) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (ContainerObjConstructProcessor constructProcessor : this.context.constructProcessors()) {
            futures.add(constructProcessor.processConstruction(instance, containerObjectResolver));
        }

        return AsyncUtils.allOf(futures).thenApply($ -> instance);
    }

    private boolean trySetLifeCycle(ContainerObj obj, LifeCycle lifeCycle) {
        if (obj.getScope() != InjectableScope.SINGLETON)
            return true;

        Class<?> type = obj.getType();
        SingletonObjectRegistry singletonObjectRegistry = this.context.singletonObjectRegistry();
        LifeCycle current = singletonObjectRegistry.getSingletonLifeCycle(type);
        if (current.isAfter(lifeCycle))
            return false;

        singletonObjectRegistry.setSingletonLifeCycle(type, lifeCycle);
        return true;
    }

    private void handleObjCollector() {
        this.node.all().forEach(obj -> handleThrow(obj, () -> this.context.objectCollectorRegistry().addToCollectors(obj)));
    }

    private <T, U> Function<T, CompletionStage<U>> directlyCompose(Supplier<CompletionStage<U>> supplier) {
        return t -> supplier.get();
    }

    private void handleThrow(ContainerObj obj, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            ContainerLogger.report(this.node, obj, throwable, "initializing");
        }
    }

    private <T> T handleThrow(ContainerObj obj, Function<ContainerObj, T> function) {
        try {
            return function.apply(obj);
        } catch (Throwable throwable) {
            ContainerLogger.report(this.node, obj, throwable, "initializing");
            return null;
        }
    }

}
