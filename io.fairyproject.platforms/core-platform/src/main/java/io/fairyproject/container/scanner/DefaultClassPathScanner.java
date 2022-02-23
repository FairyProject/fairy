package io.fairyproject.container.scanner;

import io.fairyproject.container.*;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.util.CompletableFutureUtils;
import io.fairyproject.util.SimpleTiming;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DefaultClassPathScanner extends BaseClassPathScanner {

    @Override
    public void scan() throws Exception {
        log("Start scanning containers for %s with packages [%s]... (%s)", scanName, String.join(" ", classPaths), String.join(" ", this.excludedPackages));

        this.containerObjectList.addAll(this.included);
        this.buildReflectLookup();
        this.scanClasses();
        this.initializeClasses();

        this.callInit(LifeCycle.PRE_INIT, "PRE_INIT");
        this.scanComponentAndInjection();
        this.callInit(LifeCycle.POST_INIT, "POST_INIT");
    }

    private void scanClasses() throws Exception {
        // Scanning through the JAR to see every Service ContainerObject can be registered
        try (SimpleTiming ignored = logTiming("Scanning @Service")) {
            this.scanServices(reflectLookup.findAnnotatedClasses(Service.class));
        }

        // Scanning methods that registers ContainerObject
        try (SimpleTiming ignored = logTiming("Scanning @Register")) {
            this.scanRegister(reflectLookup.findAnnotatedStaticMethods(Register.class));
        }
    }

    private void callInit(LifeCycle lifeCycle, String displayName) throws Exception {
        // Call @PreInitialize methods for ContainerObject
        try (SimpleTiming ignored = logTiming("LifeCycle " + displayName)) {
            CONTAINER_CONTEXT.lifeCycle(lifeCycle, containerObjectList);
        }
    }

    private void scanComponentAndInjection() throws Exception {
        try (SimpleTiming ignored = logTiming("Scanning Components")) {
            containerObjectList.addAll(ComponentRegistry.scanComponents(CONTAINER_CONTEXT, reflectLookup, prefix));
        }

        // Inject @Autowired fields for ContainerObjects
        try (SimpleTiming ignored = logTiming("Injecting ContainerObjects")) {
            for (ContainerObject containerObject : containerObjectList) {
                for (ContainerController controller : CONTAINER_CONTEXT.getControllers()) {
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
            for (Field field : reflectLookup.findAnnotatedStaticFields(Autowired.class)) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                AutowiredContainerController.INSTANCE.applyField(field, null);
            }
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
