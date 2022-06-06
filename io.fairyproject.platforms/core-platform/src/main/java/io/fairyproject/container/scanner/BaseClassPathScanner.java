package io.fairyproject.container.scanner;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.Register;
import io.fairyproject.container.Service;
import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.container.exception.ServiceAlreadyExistsException;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.RelativeContainerObject;
import io.fairyproject.container.object.ServiceContainerObject;
import io.fairyproject.container.object.parameter.ContainerParameterDetailsMethod;
import io.fairyproject.log.Log;
import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.NonNullArrayList;
import io.fairyproject.util.SimpleTiming;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.terminable.Terminable;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class BaseClassPathScanner extends ClassPathScanner implements Terminable {

    protected ScanResult scanResult;
    @Getter
    protected List<ContainerObject> containerObjectList = new NonNullArrayList<>();
    @Nullable
    @Getter
    private Throwable exception;

    protected CompletableFuture<ScanResult> buildClassScanner() {
        final ClassGraph classGraph = new ClassGraph()
                .enableAllInfo()
                .verbose(false);

        for (String classPath : classPaths) {
            // Only search package in the main class loader
            classGraph.acceptPackages(classPath);
        }

        for (String classPath : this.excludedPackages) {
            classGraph.rejectPackages(classPath);
        }

        if (!urls.isEmpty()) {
            classGraph.overrideClasspath(urls);
        }
        if (!classLoaders.isEmpty()) {
            classGraph.overrideClassLoaders(classLoaders.toArray(new ClassLoader[0]));
        }

        CompletableFuture<ScanResult> future = new CompletableFuture<>();
        final ListenableFuture<ScanResult> scanResultFuture = (ListenableFuture<ScanResult>) classGraph.scanAsync(ContainerContext.EXECUTOR, 4);
        scanResultFuture.addListener(() -> {
            try {
                final ScanResult scanResult = scanResultFuture.get();
                future.complete(scanResult);
            } catch (ExecutionException | InterruptedException e) {
                future.completeExceptionally(e);
            }
        }, Runnable::run);
        return future.thenApply(result -> {
            this.scanResult = result;
            return this.scanResult;
        });
    }

    protected void unregisterDisabledContainers() {
        ContainerContext containerContext = ContainerContext.get();
        containerContext.doWriteLock(() -> containerContext.getSortedObjects().addAll(containerObjectList));

        for (ContainerObject containerObject : ImmutableList.copyOf(containerObjectList)) {
            if (!containerObjectList.contains(containerObject)) {
                continue;
            }
            try {
                if (!containerObject.shouldInitialize()) {
                    log("Unregistering " + containerObject + " due to it cancelled to register");

                    containerObjectList.remove(containerObject);
                    for (ContainerObject details : containerContext.unregisterObject(containerObject)) {
                        log("Unregistering " + containerObject + " due to it dependency unregistered");

                        containerObjectList.remove(details);
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                Log.error(e);
                containerContext.unregisterObject(containerObject);
            }
        }
    }

    protected void initializeClasses() throws Exception {
        // Load ContainerObjects in Dependency Tree Order
        try (SimpleTiming ignored = logTiming("Initializing ContainerObject")) {
            initializeContainers();
        }

        // Unregistering ContainerObjects that returns false in shouldInitialize
        try (SimpleTiming ignored = logTiming("Unregistering Disabled ContainerObject")) {
            this.unregisterDisabledContainers();
        }
    }

    protected void initializeContainers() {
        final Map<Class<?>, ContainerObject> relationship = this.searchDependencyRelationship();

        // Continually loop until all dependency found and loaded
        List<ContainerObject> sorted = new NonNullArrayList<>();
        Queue<Class<?>> removeQueue = new ArrayDeque<>();
        while (!relationship.isEmpty()) {
            Iterator<Map.Entry<Class<?>, ContainerObject>> iterator = relationship.entrySet().iterator();
            List<CompletableFuture<?>> futures = new ArrayList<>();

            while (iterator.hasNext()) {
                Map.Entry<Class<?>, ContainerObject> entry = iterator.next();
                ContainerObject containerObject = entry.getValue();

                if (!this.isMissingDependencies(containerObject, relationship)) {
                    final CompletableFuture<?> future = containerObject.build(ContainerContext.get())
                            .thenRun(() -> {
                                containerObject.lifeCycle(LifeCycle.CONSTRUCT);
                                sorted.add(containerObject);
                                removeQueue.add(entry.getKey());
                            });

                    futures.add(future);
                }
            }

            final CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            try {
                all.get();
            } catch (Throwable throwable) {
                SneakyThrowUtil.sneakyThrow(throwable);
            }

            for (Class<?> key : removeQueue) {
                relationship.remove(key);
            }
            removeQueue.clear();
        }
        this.containerObjectList = sorted;
    }

    private boolean isMissingDependencies(ContainerObject containerObject, Map<Class<?>, ContainerObject> relationship) {
        boolean missingDependencies = false;

        for (Map.Entry<ServiceDependencyType, List<Class<?>>> dependencyEntry : containerObject.getDependencyEntries()) {
            final ServiceDependencyType type = dependencyEntry.getKey();
            for (Class<?> dependency : dependencyEntry.getValue()) {
                ContainerObject dependencyDetails = ContainerContext.get().getObjectDetails(dependency);
                if (dependencyDetails != null && dependencyDetails.getInstance() != null) {
                    continue;
                }
                if (type == ServiceDependencyType.SUB && !relationship.containsKey(dependency)) {
                    continue;
                }
                missingDependencies = true;
            }
        }

        return missingDependencies;
    }

    @NotNull
    protected Map<Class<?>, ContainerObject> searchDependencyRelationship() {
        ContainerContext containerContext = ContainerContext.get();
        Map<Class<?>, ContainerObject> toLoad = new HashMap<>();

        for (ContainerObject containerObject : containerObjectList) {
            toLoad.put(containerObject.getType(), containerObject);
            if (containerObject instanceof ServiceContainerObject) {
                ((ServiceContainerObject) containerObject).setupConstruction(containerContext);
            }
        }
        // Remove Services without valid dependency
        Iterator<Map.Entry<Class<?>, ContainerObject>> removeIterator = toLoad.entrySet().iterator();
        while (removeIterator.hasNext()) {
            Map.Entry<Class<?>, ContainerObject> entry = removeIterator.next();
            ContainerObject containerObject = entry.getValue();
            if (!containerObject.hasDependencies()) {
                continue;
            }
            for (Map.Entry<ServiceDependencyType, List<Class<?>>> allDependency : containerObject.getDependencyEntries()) {
                final ServiceDependencyType type = allDependency.getKey();
                search: for (Class<?> dependency : allDependency.getValue()) {
                    ContainerObject dependencyObject = containerContext.getObjectDetails(dependency);
                    if (dependencyObject == null) {
                        switch (type) {
                            default:
                            case FORCE:
                                Log.error("Couldn't find the dependency " + dependency + " for " + containerObject.getType().getSimpleName() + "!");
                                removeIterator.remove();
                                break search;
                            case SUB_DISABLE:
                                removeIterator.remove();
                                break search;
                            case SUB:
                                break;
                        }
                        // Prevent dependency each other
                    } else {
                        if (dependencyObject.hasDependencies()
                                && dependencyObject.getAllDependencies().contains(containerObject.getType())) {
                            Log.error("Target " + containerObject.getType().getSimpleName() + " and " + dependency + " depend to each other!");
                            removeIterator.remove();

                            toLoad.remove(dependency);
                            break;
                        }

                        dependencyObject.addChildren(containerObject.getType());
                    }
                }
            }
        }

        return toLoad;
    }

    protected void scanRegister(Collection<Method> registerMethods) throws InvocationTargetException, IllegalAccessException {
        ContainerContext containerContext = ContainerContext.get();

        for (Method method : registerMethods) {
            if (method.getReturnType() == void.class) {
                this.handleException(new IllegalArgumentException("The Method " + method + " has annotated @Register but no return type!"));
                return;
            }
            ContainerParameterDetailsMethod detailsMethod = new ContainerParameterDetailsMethod(method, containerContext);
            List<Class<?>> dependencies = new ArrayList<>();
            for (Parameter type : detailsMethod.getParameters()) {
                ContainerObject details = containerContext.getObjectDetails(type.getType());
                if (details != null) {
                    dependencies.add(details.getType());
                }
            }
            final Object instance = detailsMethod.invoke(null, containerContext);

            Register register = method.getAnnotation(Register.class);
            if (register == null) {
                continue;
            }

            Class<?> objectType = detailsMethod.returnType();
            if (register.as() != Void.class) {
                objectType = register.as();
            }

            if (containerContext.getObjectDetails(objectType) == null) {
                ContainerObject containerObject = new RelativeContainerObject(objectType, instance, dependencies.toArray(new Class<?>[0]));

                log("Found " + objectType + " with type " + instance.getClass().getSimpleName() + ", Registering it as ContainerObject...");

                containerContext.attemptBindPlugin(containerObject);
                containerContext.registerObject(containerObject, false);

                containerObjectList.add(containerObject);
            } else {
                this.handleException(new ServiceAlreadyExistsException(objectType));
                break;
            }
        }
    }

    protected void scanServices(Collection<Class<?>> serviceClasses) {
        ContainerContext containerContext = ContainerContext.get();

        for (Class<?> type : serviceClasses) {
            Service service = type.getDeclaredAnnotation(Service.class);
            ConditionUtils.notNull(service, "The type " + type.getName() + " doesn't have @Service annotation! " + Arrays.toString(type.getAnnotations()));

            if (containerContext.getObjectDetails(type) == null) {
                ServiceContainerObject containerObject = new ServiceContainerObject(type, service.depends());

                log("Found " + containerObject + " with type " + type.getSimpleName() + ", Registering it as ContainerObject...");

                containerContext.attemptBindPlugin(containerObject);
                containerContext.registerObject(containerObject, false);

                containerObjectList.add(containerObject);
            } else {
                new ServiceAlreadyExistsException(type).printStackTrace();
            }
        }
    }

    protected boolean handleException(Throwable throwable) {
        this.exception = throwable;
        return true;
    }

    @Override
    public void close() throws Exception {
        this.scanResult.close();
    }

    @Override
    public boolean isClosed() {
        return this.scanResult == null;
    }
}
