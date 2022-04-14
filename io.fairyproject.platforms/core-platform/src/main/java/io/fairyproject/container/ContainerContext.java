/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package io.fairyproject.container;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.controller.SubscribeEventContainerController;
import io.fairyproject.container.object.ComponentContainerObject;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.SimpleContainerObject;
import io.fairyproject.container.scanner.ClassPathScanner;
import io.fairyproject.container.scanner.DefaultClassPathScanner;
import io.fairyproject.container.scanner.ThreadedClassPathScanner;
import io.fairyproject.event.EventBus;
import io.fairyproject.event.impl.PostServiceInitialEvent;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.CompletableFutureUtils;
import io.fairyproject.util.SimpleTiming;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.thread.executor.ListeningDecorator;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ContainerContext {

    public static boolean SHOW_LOGS = Boolean.getBoolean("fairy.showlog");
    public static boolean SINGLE_THREADED = Runtime.getRuntime().availableProcessors() < 2 || Boolean.getBoolean("fairy.singlethreaded");
    public static ContainerContext INSTANCE;
    public static ListeningExecutorService EXECUTOR = ListeningDecorator.create(Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("Container Thread - %d")
            .setDaemon(true)
            .setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace())
            .build()));
    public static final int PLUGIN_LISTENER_PRIORITY = 100;

    @Getter
    private final ContainerController[] controllers = Arrays.asList(
            new AutowiredContainerController(),
            new SubscribeEventContainerController()
    ).toArray(new ContainerController[0]);

    /**
     * Lookup Storages
     */
    private final Map<Class<?>, ContainerObject> containerByType = new ConcurrentHashMap<>();

    /**
     * NOT THREAD SAFE
     */
    @Getter
    private final List<ContainerObject> sortedObjects = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Logging
     */
    public static final Logger LOGGER = LogManager.getLogger(ContainerContext.class);
    public static void log(String msg, Object... replacement) {
        if (SHOW_LOGS) {
            LOGGER.info(String.format(msg, replacement));
        }
    }
    public static void warn(String msg, Object... replacement) {
        if (SHOW_LOGS) {
            LOGGER.warn(String.format(msg, replacement));
        }
    }
    public static void fatal(String msg, Throwable e, Object... replacement) {
        if (SHOW_LOGS) {
            LOGGER.fatal(String.format(msg, replacement), e);
        }
    }
    public static SimpleTiming logTiming(String msg) {
        return SimpleTiming.create(time -> log("Ended %s - took %d ms", msg, time));
    }

    /**
     * Initializing Method for ContainerContext
     */
    public void init() {
        INSTANCE = this;

        this.registerObject(new SimpleContainerObject(this, this.getClass()));
        log("ContainerContext has been registered as ContainerObject.");

        ComponentRegistry.registerComponentHolders();
        try {
            final ClassPathScanner classPathScanner = this.scanClasses()
                    .name("framework")
                    .classPath(Fairy.getFairyPackage());
            if (!Debug.UNIT_TEST) {
                classPathScanner
                        .url(this.getClass().getProtectionDomain().getCodeSource().getLocation())
                        .classLoader(ContainerContext.class.getClassLoader());
            }
            classPathScanner.scanBlocking();

            if (classPathScanner.getException() != null) {
                SneakyThrowUtil.sneakyThrow(classPathScanner.getException());
            }
        } catch (Throwable throwable) {
            LOGGER.error("Error while scanning classes for framework", Stacktrace.simplifyStacktrace(throwable));
            Fairy.getPlatform().shutdown();
            return;
        }

        if (PluginManager.isInitialized()) {
            log("Find PluginManager, attempt to register Plugin Listeners");
            PluginManager.INSTANCE.registerListener(new ContainerPluginListener(this));
        }

        Fairy.getPlatform().onPostServicesInitial();
        EventBus.call(new PostServiceInitialEvent());
    }

    /**
     * Shutdown Method for ContainerObject Context
     */
    public void stop() {
        List<ContainerObject> detailsList = Lists.newArrayList(this.sortedObjects);
        Collections.reverse(detailsList);

        lifeCycle(LifeCycle.PRE_DESTROY, detailsList);
        for (ContainerObject containerObject : detailsList) {
            log("ContainerObject " + containerObject.getType() + " Disabled, due to framework being disabled.");

            containerObject.closeAndReportException();
        }
        lifeCycle(LifeCycle.POST_DESTROY, detailsList);
    }

    public void disableObjectUnchecked(Class<?> type) {
        this.disableObjectUnchecked(this.getObjectDetails(type));
    }

    public void disableObjectUnchecked(ContainerObject containerObject) {
        ThrowingRunnable.unchecked(() -> this.disableObject(containerObject)).run();
    }

    public void disableObject(Class<?> type) throws InvocationTargetException, IllegalAccessException {
        this.disableObject(this.getObjectDetails(type));
    }

    public void disableObject(ContainerObject containerObject) throws InvocationTargetException, IllegalAccessException {
        containerObject.lifeCycle(LifeCycle.PRE_DESTROY);
        containerObject.onDisable();
        this.unregisterObject(containerObject);
        containerObject.lifeCycle(LifeCycle.POST_DESTROY);
    }

    public ContainerObject registerObject(ContainerObject containerObject) {
        return this.registerObject(containerObject, true);
    }

    public ContainerObject registerObject(ContainerObject containerObject, boolean sort) {
        this.containerByType.put(containerObject.getType(), containerObject);
        if (sort) {
            this.sortedObjects.add(containerObject);
        }

        return containerObject;
    }

    public void doWriteLock(Runnable runnable) {
        this.lock.writeLock().lock();
        runnable.run();
        this.lock.writeLock().unlock();
    }

    public void doReadLock(Runnable runnable) {
        this.lock.readLock().lock();
        runnable.run();
        this.lock.readLock().unlock();
    }

    public Collection<ContainerObject> unregisterObject(Class<?> type) {
        return this.unregisterObject(this.getObjectDetails(type));
    }

    public Collection<ContainerObject> unregisterObject(@NonNull ContainerObject containerObject) {
        this.containerByType.remove(containerObject.getType());

        this.lock.writeLock().lock();
        this.sortedObjects.remove(containerObject);
        this.lock.writeLock().unlock();

        final ImmutableList.Builder<ContainerObject> builder = ImmutableList.builder();

        // Unregister Child Dependency
        for (Class<?> child : containerObject.getChildren()) {
            ContainerObject childContainerObject = this.getObjectDetails(child);

            builder.add(childContainerObject);
            builder.addAll(this.unregisterObject(childContainerObject));
        }

        // Remove Children from dependencies
        for (Class<?> dependency : containerObject.getAllDependencies()) {
            ContainerObject dependContainerObject = this.getObjectDetails(dependency);

            if (dependContainerObject != null) {
                dependContainerObject.removeChildren(containerObject.getType());
            }
        }

        return builder.build();
    }

    public ContainerObject getObjectDetails(Class<?> type) {
        return this.containerByType.get(type);
    }

    public Object getContainerObject(@NonNull Class<?> type) {
        ContainerObject details = this.getObjectDetails(type);
        if (details == null) {
            return null;
        }
        return details.getInstance();
    }

    public boolean isRegisteredObject(Class<?>... types) {
        for (Class<?> type : types) {
            ContainerObject dependencyDetails = this.getObjectDetails(type);
            if (dependencyDetails == null || dependencyDetails.getInstance() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isObject(Class<?> objectClass) {
        return this.containerByType.containsKey(objectClass);
    }

    public boolean isObject(Object object) {
        return this.isObject(object.getClass());
    }

    public Collection<ContainerObject> findDetailsBindWith(Plugin plugin) {
        return this.containerByType.values()
                .stream()
                .filter(containerObject -> containerObject.isBind() && containerObject.getBindPlugin().equals(plugin))
                .collect(Collectors.toList());
    }

    /**
     * Registration
     */

    public ComponentContainerObject registerComponent(Object instance, String prefix, Class<?> type, ComponentHolder componentHolder) throws InvocationTargetException, IllegalAccessException {
        Component component = type.getAnnotation(Component.class);
        if (component == null) {
            throw new IllegalArgumentException("The type " + type.getName() + " doesn't have Component annotation!");
        }

        ServiceDependency serviceDependency = type.getAnnotation(ServiceDependency.class);
        if (serviceDependency != null) {
            for (Class<?> dependency : serviceDependency.value()) {
                if (!this.isRegisteredObject(dependency)) {
                    switch (serviceDependency.type()) {
                        case FORCE:
                            LOGGER.error("Couldn't find the dependency " + dependency + " for " + type.getSimpleName() + "!");
                        case SUB_DISABLE:
                            return null;
                        case SUB:
                            break;
                    }
                }
            }
        }

        ComponentContainerObject containerObject = new ComponentContainerObject(type, instance, componentHolder);
        containerObject.lifeCycle(LifeCycle.CONSTRUCT);
        if (!containerObject.shouldInitialize()) {
            return null;
        }

        this.registerObject(containerObject);
        this.attemptBindPlugin(containerObject);

        try {
            containerObject.lifeCycle(LifeCycle.PRE_INIT);
        } catch (Throwable throwable) {
            LOGGER.error(throwable);
        }
        return containerObject;
    }

    public void attemptBindPlugin(ContainerObject containerObject) {
        if (PluginManager.isInitialized()) {
            Plugin plugin = PluginManager.INSTANCE.getPluginByClass(containerObject.getType());

            if (plugin != null) {
                containerObject.bindWith(plugin);

                log("ContainerObject " + containerObject.getType() + " is now bind with plugin " + plugin.getName());
            }
        }
    }

    public void lifeCycle(LifeCycle lifeCycle, Collection<ContainerObject> containerObjectList) {
        this.lifeCycleAsynchronously(lifeCycle, containerObjectList).join();
    }

    public CompletableFuture<?> lifeCycleAsynchronously(LifeCycle lifeCycle, Collection<ContainerObject> containerObjectList) {
        List<CompletableFuture<?>> futures = new ArrayList<>(containerObjectList.size());

        for (ContainerObject containerObject : containerObjectList) {
            try {
                futures.add(containerObject.lifeCycle(lifeCycle));
            } catch (Throwable throwable) {
                futures.clear();
                return CompletableFutureUtils.failureOf(throwable);
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public List<String> findClassPaths(Class<?> plugin) {
        ClasspathScan annotation = plugin.getAnnotation(ClasspathScan.class);

        if (annotation != null) {
            return Lists.newArrayList(annotation.value());
        }

        return Lists.newArrayList();
    }

    public ClassPathScanner scanClasses() {
        return SINGLE_THREADED ? new DefaultClassPathScanner() : new ThreadedClassPathScanner();
    }

}
