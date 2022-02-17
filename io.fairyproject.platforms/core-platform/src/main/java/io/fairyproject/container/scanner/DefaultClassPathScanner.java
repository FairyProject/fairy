package io.fairyproject.container.scanner;

import io.fairyproject.container.*;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.util.SimpleTiming;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DefaultClassPathScanner extends BaseClassPathScanner {

    private boolean success = false;

    @Override
    public void scan() throws Exception {
        log("Start scanning containers for %s with packages [%s]... (%s)", scanName, String.join(" ", classPaths), String.join(" ", this.excludedPackages));

        // Build the instance for Reflection Lookup
        try (SimpleTiming ignored = logTiming("Reflect Lookup building")) {
            this.buildReflectLookup();
        }

        this.containerObjectList.addAll(this.included);
        // Scanning through the JAR to see every Service ContainerObject can be registered
        try (SimpleTiming ignored = logTiming("Scanning @Service")) {
            this.scanServices(reflectLookup.findAnnotatedClasses(Service.class));
        }

        // Scanning methods that registers ContainerObject
        try (SimpleTiming ignored = logTiming("Scanning @Register")) {
            this.scanRegister(reflectLookup.findAnnotatedStaticMethods(Register.class));
        }

        // Load ContainerObjects in Dependency Tree Order
        try (SimpleTiming ignored = logTiming("Initializing ContainerObject")) {
            containerObjectList = initializeContainers();
            if (containerObjectList == null) {
                return;
            }
        }

        // Unregistering ContainerObjects that returns false in shouldInitialize
        try (SimpleTiming ignored = logTiming("Unregistering Disabled ContainerObject")) {
            this.unregisterDisabledContainers();
        }

        // Call @PreInitialize methods for ContainerObject
        try (SimpleTiming ignored = logTiming("LifeCycle PRE_INIT")) {
            CONTAINER_CONTEXT.lifeCycle(LifeCycle.PRE_INIT, containerObjectList);
        }

        // Scan Components
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

        // Call @PostInitialize
        try (SimpleTiming ignored = logTiming("LifeCycle POST_INIT")) {
            CONTAINER_CONTEXT.lifeCycle(LifeCycle.POST_INIT, containerObjectList);
        }

        this.success = true;
    }

    @Override
    public CompletableFuture<List<ContainerObject>> getCompletedFuture() {
        if (this.success)
            return CompletableFuture.completedFuture(this.containerObjectList);
        else
            return new CompletableFuture<>(); // TODO
    }
}
