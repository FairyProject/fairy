package io.fairyproject.container.scanner;

import io.fairyproject.container.*;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.thread.BlockingThreadAwaitQueue;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadedClassPathScanner extends BaseClassPathScanner {

    @Getter
    private final CompletableFuture<List<ContainerObject>> completedFuture = new CompletableFuture<>();

    private Collection<Class<?>> serviceClasses;
    private BlockingThreadAwaitQueue main;

    @Override
    public void scan() throws Exception {
        log("Start scanning containers for %s with packages [%s]... (%s)", scanName, String.join(" ", classPaths), String.join(" ", this.excludedPackages));

        this.containerObjectList.addAll(this.included);
        this.main = BlockingThreadAwaitQueue.create(this::handleException);

        // Build the instance for Reflection Lookup
        this.buildReflectLookup();
        this.scanClasses()
                .thenRunAsync(ThrowingRunnable.sneaky(this::initializeClasses), this.main)
                .thenComposeAsync(this.directlyCompose(() -> this.callInit(LifeCycle.PRE_INIT)), this.main)
                .thenCompose(this.directlyCompose(this::scanComponentAndInjection))
                .thenComposeAsync(this.directlyCompose(() -> this.callInit(LifeCycle.POST_INIT)), this.main)
                .whenComplete(this.whenComplete(() -> this.completedFuture.complete(this.containerObjectList)));
    }

    @Override
    public void scanBlocking() throws Exception {
        this.scan();

        this.main.await(() -> this.getCompletedFuture().isDone());
    }

    private CompletableFuture<?> applyAutowiredStaticFields() {
        return CompletableFuture.runAsync(() -> {
            for (Field field : reflectLookup.findAnnotatedStaticFields(Autowired.class)) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                try {
                    AutowiredContainerController.INSTANCE.applyField(field, null);
                } catch (ReflectiveOperationException e) {
                    SneakyThrowUtil.sneakyThrow(e);
                }
            }
        }, ContainerContext.EXECUTOR);
    }

    public CompletableFuture<?> scanComponentAndInjection() {
        containerObjectList.addAll(ComponentRegistry.scanComponents(CONTAINER_CONTEXT, reflectLookup, prefix));
        final CompletableFuture<?> future = this.applyAutowiredStaticFields();

        this.applyControllers();
        return future.thenRun(() -> containerObjectList.forEach(ContainerObject::onEnable));
    }

    private CompletableFuture<?> callInit(LifeCycle lifeCycle) {
        return CONTAINER_CONTEXT.lifeCycleAsynchronously(lifeCycle, containerObjectList);
    }

    public CompletableFuture<?> scanClasses() {
        CompletableFuture<?>[] futures = new CompletableFuture[2];
        futures[0] = CompletableFuture.runAsync(() -> {
            this.serviceClasses = reflectLookup.findAnnotatedClasses(Service.class);
            this.scanServices(this.serviceClasses);
        }, ContainerContext.EXECUTOR);
        futures[1] = CompletableFuture.runAsync(() -> {
            try {
                this.scanRegister(reflectLookup.findAnnotatedStaticMethods(Register.class));
            } catch (Throwable throwable) {
                SneakyThrowUtil.sneakyThrow(throwable);
            }
        }, ContainerContext.EXECUTOR);

        return CompletableFuture.allOf(futures);
    }

    public void applyControllers() {
        this.applyControllers(CONTAINER_CONTEXT.getControllers());
    }

    public void applyControllers(ContainerController[] controllers) {
        containerObjectList.parallelStream()
                .forEach(containerObject -> {
                    for (ContainerController controller : controllers) {
                        try {
                            controller.applyContainerObject(containerObject);
                        } catch (Throwable throwable) {
                            ContainerContext.LOGGER.warn("An error occurs while apply controller for " + containerObject.getType(), throwable);
                        }
                    }
                });
    }

    @Override
    protected boolean handleException(Throwable throwable) {
        super.handleException(throwable);
        this.completedFuture.completeExceptionally(Stacktrace.simplifyStacktrace(throwable));
        return true;
    }

    private <T> BiConsumer<T, Throwable> whenComplete(Runnable onSuccess) {
        return (ignored, throwable) -> {
            if (throwable != null) {
                this.handleException(throwable);
            } else {
                onSuccess.run();
            }
        };
    }

    private <T, U> Function<T, CompletionStage<U>> directlyCompose(Supplier<CompletionStage<U>> supplier) {
        return t -> supplier.get();
    }
}
