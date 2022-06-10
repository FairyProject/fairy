package io.fairyproject.container.scanner;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import io.fairyproject.Debug;
import io.fairyproject.container.*;
import io.fairyproject.container.exception.ServiceAlreadyExistsException;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.RelativeContainerObj;
import io.fairyproject.container.object.ServiceContainerObj;
import io.fairyproject.container.object.parameter.MethodContainerResolver;
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
        containerContext.doWriteLock(() -> containerContext.getSortedObjects().addAll(containerObjList));

        for (ContainerObj containerObj : ImmutableList.copyOf(containerObjList)) {
            if (!containerObjList.contains(containerObj)) {
                continue;
            }
            try {
                if (!containerObj.shouldActive()) {
                    log("Unregistering " + containerObj + " due to it cancelled to register");

                    containerObjList.remove(containerObj);
                    for (ContainerObj details : containerContext.unregisterObject(containerObj)) {
                        log("Unregistering " + containerObj + " due to it dependency unregistered");

                        containerObjList.remove(details);
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                Log.error(e);
                containerContext.unregisterObject(containerObj);
            }
        }
    }

    protected void initializeClasses() throws Exception {
        // Load ContainerObjects in Dependency Tree Order
        try (SimpleTiming ignored = logTiming("Initializing ContainerObject")) {
            this.initializeContainers();
        }

        // Unregistering ContainerObjects that returns false in shouldInitialize
        try (SimpleTiming ignored = logTiming("Unregistering Disabled ContainerObject")) {
            this.unregisterDisabledContainers();
        }
    }

    protected void initializeContainers() {
        final Map<Class<?>, ContainerObj> relationship = this.searchDependencyRelationship();

        // Continually loop until all dependency found and loaded
        List<ContainerObj> sorted = new NonNullArrayList<>();
        Queue<Class<?>> removeQueue = new ArrayDeque<>();
        while (!relationship.isEmpty()) {
            Iterator<Map.Entry<Class<?>, ContainerObj>> iterator = relationship.entrySet().iterator();
            List<CompletableFuture<?>> futures = new ArrayList<>();

            while (iterator.hasNext()) {
                Map.Entry<Class<?>, ContainerObj> entry = iterator.next();
                ContainerObj containerObj = entry.getValue();

                if (!this.isMissingDependencies(containerObj, relationship)) {
                    final CompletableFuture<?> future = containerObj.construct(ContainerContext.get())
                            .thenRun(() -> {
                                containerObj.lifeCycle(LifeCycle.CONSTRUCT);
                                sorted.add(containerObj);
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
        this.containerObjList = sorted;
    }

    private boolean isMissingDependencies(ContainerObj containerObj, Map<Class<?>, ContainerObj> relationship) {
        boolean missingDependencies = false;

        for (Map.Entry<ServiceDependencyType, List<Class<?>>> dependencyEntry : containerObj.getDependEntries()) {
            final ServiceDependencyType type = dependencyEntry.getKey();
            for (Class<?> dependency : dependencyEntry.getValue()) {
                ContainerObj dependencyDetails = ContainerRef.getObj(dependency);
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
    protected Map<Class<?>, ContainerObj> searchDependencyRelationship() {
        ContainerContext containerContext = ContainerContext.get();
        Map<Class<?>, ContainerObj> toLoad = new HashMap<>();

        for (ContainerObj containerObj : containerObjList) {
            toLoad.put(containerObj.getType(), containerObj);
            if (containerObj instanceof ServiceContainerObj) {
                ((ServiceContainerObj) containerObj).setupConstruction(containerContext);
            }
        }
        // Remove Services without valid dependency
        Iterator<Map.Entry<Class<?>, ContainerObj>> removeIterator = toLoad.entrySet().iterator();
        while (removeIterator.hasNext()) {
            Map.Entry<Class<?>, ContainerObj> entry = removeIterator.next();
            ContainerObj containerObj = entry.getValue();
            if (!containerObj.hasDepend()) {
                continue;
            }
            for (Map.Entry<ServiceDependencyType, List<Class<?>>> allDependency : containerObj.getDependEntries()) {
                final ServiceDependencyType type = allDependency.getKey();
                search: for (Class<?> dependency : allDependency.getValue()) {
                    ContainerObj dependencyObject = containerContext.getObjectDetails(dependency);
                    if (dependencyObject == null) {
                        switch (type) {
                            default:
                            case FORCE:
                                Log.error("Couldn't find the dependency " + dependency + " for " + containerObj.getType().getSimpleName() + "!");
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
                        if (dependencyObject.hasDepend()
                                && dependencyObject.getDepends().contains(containerObj.getType())) {
                            Log.error("Target " + containerObj.getType().getSimpleName() + " and " + dependency + " depend to each other!");
                            removeIterator.remove();

                            toLoad.remove(dependency);
                            break;
                        }

                        dependencyObject.addChild(containerObj.getType());
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
            MethodContainerResolver detailsMethod = new MethodContainerResolver(method, containerContext);
            List<Class<?>> dependencies = new ArrayList<>();
            for (Parameter type : detailsMethod.getParameters()) {
                ContainerObj details = containerContext.getObjectDetails(type.getType());
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
                ContainerObj containerObj = new RelativeContainerObj(objectType, instance, dependencies.toArray(new Class<?>[0]));

                log("Found " + objectType + " with type " + instance.getClass().getSimpleName() + ", Registering it as ContainerObject...");

                containerContext.attemptBindPlugin(containerObj);
                containerContext.registerObject(containerObj, false);

                containerObjList.add(containerObj);
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

            if (ContainerRef.getObj(type) == null) {
                ServiceContainerObj containerObject = new ServiceContainerObj(type, service.depends());

                log("Found " + containerObject + " with type " + type.getSimpleName() + ", Registering it as ContainerObject...");

                containerContext.attemptBindPlugin(containerObject);
                this.node.addObj(containerObject);

                containerObjList.add(containerObject);
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
