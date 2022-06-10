package io.fairyproject.container.scanner;

import io.fairyproject.container.*;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.log.Log;
import io.fairyproject.util.ClassGraphUtil;
import io.fairyproject.util.AsyncUtils;
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
            this.node.all().forEach(obj -> obj.lifeCycle(lifeCycle));
        }
    }

    private void scanComponentAndInjection() throws Exception {
        try (SimpleTiming ignored = logTiming("Scanning Components")) {
            containerObjList.addAll(ComponentRegistry.scanComponents(ContainerContext.get(), scanResult, prefix));
        }

        // Inject @Autowired fields for ContainerObjects
        try (SimpleTiming ignored = logTiming("Injecting ContainerObjects")) {
            for (ContainerObj containerObj : containerObjList) {
                for (ContainerController controller : ContainerContext.get().getControllers()) {
                    try {
                        controller.applyContainerObject(containerObj);
                    } catch (Throwable throwable) {
                        Log.warn("An error occurs while apply controller for " + containerObject.getType(), throwable);
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
            containerObjList.forEach(ContainerObj::onEnable);
        }
    }

    @Override
    public CompletableFuture<List<ContainerObj>> getCompletedFuture() {
        if (this.getException() != null) {
            return AsyncUtils.failureOf(this.getException());
        }
        return CompletableFuture.completedFuture(this.containerObjList);
    }
}
