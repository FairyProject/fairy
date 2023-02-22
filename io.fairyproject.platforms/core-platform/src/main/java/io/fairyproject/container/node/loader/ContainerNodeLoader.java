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
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.controller.node.NodeController;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.provider.InstanceProvider;
import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.thread.BlockingThreadAwaitQueue;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ContainerNodeLoader {

    private final ContainerContext context;
    private final ContainerNode node;

    public void load() {
        BlockingThreadAwaitQueue queue = BlockingThreadAwaitQueue.create();

        this.node.resolve();
        CompletableFuture<?> completableFuture = this.initProvider()
                .thenCompose(directlyCompose(this::initLifeCycleHandlers))
                .thenComposeAsync(directlyCompose(() -> this.handleLifeCycle(LifeCycle.CONSTRUCT)), queue)
                .thenCompose(directlyCompose(this::handleController))
                .thenComposeAsync(directlyCompose(() -> this.handleLifeCycle(LifeCycle.PRE_INIT)), queue)
                .thenRun(this::handleObjCollector)
                .thenComposeAsync(directlyCompose(() -> this.handleLifeCycle(LifeCycle.POST_INIT)), queue);

        queue.await(completableFuture::isDone);
        ThrowingRunnable.sneaky(completableFuture::get).run();
    }

    private CompletableFuture<?> initProvider() {
        return this.node.forEachClockwiseAwait(this::initProviderForComponent);
    }

    private CompletableFuture<?> initProviderForComponent(ContainerObj containerObj) {
        InstanceProvider instanceProvider = containerObj.provider();
        if (instanceProvider == null)
            return AsyncUtils.empty();

        return containerObj.threadingMode().execute(() -> {
            Object instance;
            try {
                instance = instanceProvider.provide();
            } catch (Exception e) {
                SneakyThrowUtil.sneakyThrow(e);
                return;
            }

            containerObj.setInstance(instance);
        });
    }

    private CompletableFuture<?> initLifeCycleHandlers() {
        return AsyncUtils.allOf(this.node.all().stream()
                .map(ContainerObj::initLifeCycleHandlers)
                .collect(Collectors.toList()));
    }

    private CompletableFuture<?> handleLifeCycle(LifeCycle lifeCycle) {
        if (lifeCycle.isReverseOrder()) {
            return this.node.forEachCounterClockwiseAwait(obj -> handleLifeCycleForComponent(obj, lifeCycle));
        } else {
            return this.node.forEachClockwiseAwait(obj -> handleLifeCycleForComponent(obj, lifeCycle));
        }
    }

    private CompletableFuture<?> handleLifeCycleForComponent(ContainerObj obj, LifeCycle lifeCycle) {
        try {
            return obj.setLifeCycle(lifeCycle);
        } catch (Throwable throwable) {
            ContainerLogger.reportComponent(obj, "initializing life cycle " + lifeCycle, throwable);
            return AsyncUtils.failureOf(throwable);
        }
    }

    private void handleObjCollector() {
        this.node.all().forEach(obj -> handleThrow(obj, () -> this.context.objectCollectorRegistry().collect(obj)));
    }

    private CompletableFuture<?> handleController() {
        for (NodeController nodeController : this.node.controllers()) {
            nodeController.onInit();
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (ContainerController controller : ContainerContext.get().controllers()) {
            futures.add(this.node.forEachClockwiseAwait(obj -> {
                try {
                    return this.handleControllerForObject(controller, obj);
                } catch (Throwable throwable) {
                    ContainerLogger.reportComponent(obj, "initializing controller", throwable);
                    return AsyncUtils.failureOf(throwable);
                }
            }));
        }

        return AsyncUtils.allOf(futures);
    }

    private CompletableFuture<?> handleControllerForObject(ContainerController controller, ContainerObj obj) {
        return CompletableFuture.runAsync(() -> {
            try {
                controller.applyContainerObject(obj);
            } catch (Throwable throwable) {
                ContainerLogger.reportComponent(obj, "Applying controller", throwable);
                SneakyThrowUtil.sneakyThrow(throwable);
            }
        });
    }

    private <T, U> Function<T, CompletionStage<U>> directlyCompose(Supplier<CompletionStage<U>> supplier) {
        return t -> supplier.get();
    }

    private void handleThrow(ContainerObj obj, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            ContainerLogger.reportComponent(obj, "initializing", throwable);
        }
    }

    private <T> T handleThrow(ContainerObj obj, Function<ContainerObj, T> function) {
        try {
            return function.apply(obj);
        } catch (Throwable throwable) {
            ContainerLogger.reportComponent(obj, "initializing", throwable);
            return null;
        }
    }

}
