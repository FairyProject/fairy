package io.fairyproject.container.scanner;

import io.fairyproject.container.*;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.util.SimpleTiming;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ThreadedClassPathScanner extends BaseClassPathScanner {

    @Getter
    private final CompletableFuture<List<ContainerObject>> completedFuture = new CompletableFuture<>();
    private Collection<Class<?>> serviceClasses;

    @Override
    public void scan() throws Exception {
        log("Start scanning containers for %s with packages [%s]... (%s)", scanName, String.join(" ", classPaths), String.join(" ", this.excludedPackages));

        this.containerObjectList.addAll(this.included);

        // Build the instance for Reflection Lookup
        try (SimpleTiming ignored = logTiming("Reflect Lookup building")) {
            this.buildReflectLookup();
        }

        CompletableFuture<?>[] futures = new CompletableFuture[2];
        for (int i = 0; i < futures.length; i++) {
            futures[i] = new CompletableFuture<>();
        }

        ContainerContext.EXECUTOR.submit(() -> {
            try {
                this.serviceClasses = reflectLookup.findAnnotatedClasses(Service.class);
                this.scanServices(this.serviceClasses);

                futures[0].complete(null);
            } catch (Throwable throwable) {
                futures[0].completeExceptionally(throwable);
            }
        });

        ContainerContext.EXECUTOR.submit(() -> {
            try {
                this.scanRegister(reflectLookup.findAnnotatedStaticMethods(Register.class));

                futures[1].complete(null);
            } catch (Throwable throwable) {
                futures[1].completeExceptionally(throwable);
            }
        });

        final CompletableFuture<Void> all = CompletableFuture.allOf(futures);
        all.whenComplete((ignored, ex) -> {
            if (ex != null) {
                this.completedFuture.completeExceptionally(ex);
            } else {
                this.initializeContainers();
                this.unregisterDisabledContainers();

                CONTAINER_CONTEXT.lifeCycle(LifeCycle.PRE_INIT, containerObjectList);
                containerObjectList.addAll(ComponentRegistry.scanComponents(CONTAINER_CONTEXT, reflectLookup, prefix));

                final Future<Throwable> future = ContainerContext.EXECUTOR.submit(() -> {
                    try {
                        for (Field field : reflectLookup.findAnnotatedStaticFields(Autowired.class)) {
                            if (!Modifier.isStatic(field.getModifiers())) {
                                continue;
                            }

                            AutowiredContainerController.INSTANCE.applyField(field, null);
                        }
                    } catch (Throwable throwable) {
                        return throwable;
                    }
                    return null;
                });

                this.applyControllers();
                try {
                    final Throwable throwable = future.get();
                    if (throwable != null) {
                        completedFuture.completeExceptionally(throwable);
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    completedFuture.completeExceptionally(e);
                    return;
                }

                containerObjectList.forEach(ContainerObject::onEnable);
                CONTAINER_CONTEXT.lifeCycleAsynchronously(LifeCycle.POST_INIT, containerObjectList)
                        .whenComplete((o, throwable) -> {
                            if (throwable != null) {
                                completedFuture.completeExceptionally(throwable);
                            } else {
                                completedFuture.complete(containerObjectList);
                            }
                        });
            }
        });
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
}
