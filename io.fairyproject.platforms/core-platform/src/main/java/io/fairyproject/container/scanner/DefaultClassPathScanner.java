package io.fairyproject.container.scanner;

import io.fairyproject.container.*;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.util.ClassGraphUtil;
import io.fairyproject.util.CompletableFutureUtils;
import io.fairyproject.util.SimpleTiming;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DefaultClassPathScanner extends BaseClassPathScanner {

    @Override
    public void scan() throws Exception {
        log("Start scanning containers for %s with packages [%s]... (%s)", scanName, String.join(" ", classPaths), String.join(" ", this.excludedPackages));

        this.containerObjectList.addAll(this.included);
        try (SimpleTiming ignored = logTiming("Reflect Lookup building")) {
            this.buildClassScanner().join();
        }
        this.scanClasses();
        this.initializeClasses();

        this.callInit(LifeCycle.PRE_INIT, "PRE_INIT");
        this.scanComponentAndInjection();
        this.callInit(LifeCycle.POST_INIT, "POST_INIT");
    }

    private void scanClasses() throws Exception {
        // Scanning through the JAR to see every Service ContainerObject can be registered
        try (SimpleTiming ignored = logTiming("Scanning @Service")) {
            this.scanServices(scanResult.getClassesWithAnnotation(Service.class).loadClasses());
        }

        // Scanning methods that registers ContainerObject
        try (SimpleTiming ignored = logTiming("Scanning @Register")) {
            this.scanRegister(ClassGraphUtil.methodWithAnnotation(scanResult, Register.class).filter(method -> Modifier.isStatic(method.getModifiers())).collect(Collectors.toList()));
        }
    }

    private void callInit(LifeCycle lifeCycle, String displayName) throws Exception {
        // Call @PreInitialize methods for ContainerObject
        try (SimpleTiming ignored = logTiming("LifeCycle " + displayName)) {
            ContainerContext.get().lifeCycle(lifeCycle, containerObjectList);
        }
    }

    private void scanComponentAndInjection() throws Exception {
        try (SimpleTiming ignored = logTiming("Scanning Components")) {
            containerObjectList.addAll(ComponentRegistry.scanComponents(ContainerContext.get(), scanResult, prefix));
        }

        // Inject @Autowired fields for ContainerObjects
        try (SimpleTiming ignored = logTiming("Injecting ContainerObjects")) {
            for (ContainerObject containerObject : containerObjectList) {
                for (ContainerController controller : ContainerContext.get().getControllers()) {
                    try {
                        controller.applyContainerObject(containerObject);
                    } catch (Throwable throwable) {
                        ContainerContext.LOGGER.warn("An error occurs while apply controller for " + containerObject.getType(), throwable);
                    }
                }
            }
        }

        // Inject @Autowired static fields
        try (SimpleTiming ignored = logTiming("Injecting Static Autowired Fields")) {
            ClassGraphUtil.fieldWithAnnotation(scanResult, Autowired.class)
                    .filter(field -> Modifier.isStatic(field.getModifiers()))
                    .forEach(field -> {
                        if (!Modifier.isStatic(field.getModifiers())) {
                            return;
                        }

                        try {
                            AutowiredContainerController.INSTANCE.applyField(field, null);
                        } catch (ReflectiveOperationException e) {
                            SneakyThrowUtil.sneakyThrow(e);
                        }
                    });
        }

        // Call onEnable() for Components
        try (SimpleTiming ignored = logTiming("Call onEnable() for Components")) {
            containerObjectList.forEach(ContainerObject::onEnable);
        }
    }

    @Override
    public CompletableFuture<List<ContainerObject>> getCompletedFuture() {
        if (this.getException() != null) {
            return CompletableFutureUtils.failureOf(this.getException());
        }
        return CompletableFuture.completedFuture(this.containerObjectList);
    }
}
