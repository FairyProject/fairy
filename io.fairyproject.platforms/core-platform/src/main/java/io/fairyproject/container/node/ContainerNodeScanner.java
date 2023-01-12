package io.fairyproject.container.node;

import io.fairyproject.Debug;
import io.fairyproject.container.*;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.exception.ServiceAlreadyExistsException;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.Obj;
import io.fairyproject.container.object.lifecycle.impl.provider.LifeCycleHandlerConstructorProvider;
import io.fairyproject.container.object.lifecycle.impl.provider.LifeCycleHandlerMethodProvider;
import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.ClassGraphUtil;
import io.fairyproject.util.SimpleTiming;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.thread.BlockingThreadAwaitQueue;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ContainerNodeScanner {

    protected String prefix = "";
    protected String scanName;

    protected ContainerNode mainNode;
    protected ContainerNode objNode;

    protected final List<String> classPaths = new ArrayList<>();
    protected final List<String> excludedPackages = new ArrayList<>();
    protected final List<URL> urls = new ArrayList<>();
    protected final List<ClassLoader> classLoaders = new ArrayList<>();

    protected final List<ContainerObj> included = new ArrayList<>();

    protected ScanResult scanResult;

    public static void log(String msg, Object... replacement) {
        Debug.log(msg, replacement);
    }

    public static SimpleTiming logTiming(String msg) {
        return Debug.logTiming(msg);
    }

    public void prefix(String prefix) {
        this.prefix = prefix;
    }

    public void name(String name) {
        this.scanName = name;
    }

    public void node(ContainerNode node) {
        this.mainNode = node;
    }

    public void classPath(String... classPath) {
        this.classPaths.addAll(Arrays.asList(classPath));
    }

    public void classPath(Collection<String> classPath) {
        this.classPaths.addAll(classPath);
    }

    public void url(URL... urls) {
        this.urls.addAll(Arrays.asList(urls));
    }

    public void excludePackage(String... classPath) {
        this.excludedPackages.addAll(Arrays.asList(classPath));
    }

    public void excludePackage(Collection<String> classPath) {
        this.excludedPackages.addAll(classPath);
    }

    public void classLoader(ClassLoader... classLoaders) {
        this.classLoaders.addAll(Arrays.asList(classLoaders));
    }

    public void classLoader(Collection<ClassLoader> classLoaders) {
        this.classLoaders.addAll(classLoaders);
    }

    public ContainerNode scan() {
        if (this.mainNode == null)
            this.mainNode = ContainerNode.create(this.scanName);
        this.objNode = ContainerNode.create(this.scanName + ":obj");

        this.mainNode.addChild(this.objNode);

        BlockingThreadAwaitQueue queue = BlockingThreadAwaitQueue.create();

        try {
            final CompletableFuture<?> future = CompletableFuture.runAsync(this::buildClassScanner, ContainerContext.get().executor)
                    .thenRun(this::loadServiceClasses)
                    .thenRun(this::loadRegister)
                    .thenRun(this::loadObjClasses)
                    .thenCompose(directlyCompose(this::initLifeCycleHandlers))
                    .thenCompose(directlyCompose(this::resolveGraph))
                    .thenComposeAsync(directlyCompose(() -> this.handleLifeCycle(LifeCycle.CONSTRUCT)), queue)
                    .thenCompose(directlyCompose(this::handleController))
                    .thenComposeAsync(directlyCompose(() -> this.handleLifeCycle(LifeCycle.PRE_INIT)), queue)
                    .thenRun(this::handleObjCollector)
                    .thenComposeAsync(directlyCompose(() -> this.handleLifeCycle(LifeCycle.POST_INIT)), queue)
                    .thenRun(() -> this.scanResult.close());

            queue.await(future::isDone);
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            SneakyThrowUtil.sneakyThrow(e);
        }

        return this.mainNode;
    }

    private void handleObjCollector() {
        this.objNode.all().forEach(ContainerContext.get().objectCollectorRegistry()::collect);
    }

    private CompletableFuture<?> initLifeCycleHandlers() {
        return AsyncUtils.allOf(this.mainNode.all().stream()
                .map(ContainerObj::initLifeCycleHandlers)
                .collect(Collectors.toList()));
    }

    private CompletableFuture<?> handleController() {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (ContainerController controller : ContainerContext.get().controllers()) {
            controller.init(this.scanResult);
            futures.add(this.mainNode.graph().forEachClockwiseAwait(obj -> this.handleControllerForObject(controller, obj)));
            futures.add(this.objNode.graph().forEachClockwiseAwait(obj -> this.handleControllerForObject(controller, obj)));
        }

        return AsyncUtils.allOf(futures);
    }

    private CompletableFuture<?> handleControllerForObject(ContainerController controller, ContainerObj obj) {
        return CompletableFuture.runAsync(ThrowingRunnable.sneaky(() -> controller.applyContainerObject(obj)));
    }

    private CompletableFuture<?> resolveGraph() {
        return CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> this.mainNode.resolve()),
                CompletableFuture.runAsync(() -> this.objNode.resolve())
        );
    }

    private void loadObjClasses() {
        final ClassInfoList objClasses = this.scanResult.getClassesWithAnnotation(Obj.class);
        objClasses.loadClasses().forEach(aClass -> this.handleLoadClass(aClass, new Class<?>[0], this.objNode, true));
    }

    private CompletableFuture<?> handleLifeCycle(LifeCycle lifeCycle) {
        if (lifeCycle.isReverseOrder()) {
            return CompletableFuture.allOf(
                    this.mainNode.graph().forEachCounterClockwiseAwait(obj -> obj.setLifeCycle(lifeCycle)),
                    this.objNode.graph().forEachCounterClockwiseAwait(obj -> obj.setLifeCycle(lifeCycle))
            );
        } else {
            return CompletableFuture.allOf(
                    this.mainNode.graph().forEachClockwiseAwait(obj -> obj.setLifeCycle(lifeCycle)),
                    this.objNode.graph().forEachClockwiseAwait(obj -> obj.setLifeCycle(lifeCycle))
            );
        }
    }

    private void loadRegister() {
        ClassGraphUtil.methodWithAnnotation(scanResult, Register.class)
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> {
                    Register register = method.getAnnotation(Register.class);
                    if (register == null)
                        return;
                    if (method.getReturnType() == void.class)
                        throw new IllegalArgumentException("The Method " + method + " has annotated @Register but no return type!");

                    Class<?> objectType = method.getReturnType();
                    if (register.as() != Void.class)
                        objectType = register.as();

                    ContainerObj containerObj = this.handleLoadClass(objectType, method.getParameterTypes(), this.mainNode, false);
                    containerObj.addLifeCycleHandler(new LifeCycleHandlerMethodProvider(ContainerContext.get(), containerObj, method));
                    this.mainNode.addObj(containerObj);
                });
    }

    private void loadServiceClasses() {
        final ClassInfoList serviceClasses = this.scanResult.getClassesWithAnnotation(Service.class);
        serviceClasses.loadClasses().forEach(aClass -> {
            Service service = aClass.getDeclaredAnnotation(Service.class);
            if (service == null)
                return;
            this.handleLoadClass(aClass, service.depends(), this.mainNode, true);
        });
    }

    private ContainerObj handleLoadClass(Class<?> aClass, Class<?>[] depends, ContainerNode node, boolean constructProvider) {
        if (ContainerRef.hasObj(aClass))
            throw new ServiceAlreadyExistsException(aClass);

        ContainerObj containerObject = ContainerObj.of(aClass);

        for (Class<?> depend : depends)
            containerObject.addDepend(depend, ServiceDependencyType.FORCE);
        ContainerContext.get()
                .lifeCycleHandlerRegistry()
                .handle(containerObject);
        if (constructProvider)
            containerObject.addLifeCycleHandler(new LifeCycleHandlerConstructorProvider(containerObject));

        node.addObj(containerObject);

        return containerObject;
    }

    protected void buildClassScanner() {
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

        this.scanResult = classGraph.scan(ContainerContext.get().executor(), 4);
    }

    private void onScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    private <T, U> Function<T, CompletionStage<U>> directlyCompose(Supplier<CompletionStage<U>> supplier) {
        return t -> supplier.get();
    }
}
