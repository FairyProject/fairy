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

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.controller.SubscribeEventContainerController;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ComponentContainerObj;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.SimpleContainerObj;
import io.fairyproject.container.scanner.ClassPathScanner;
import io.fairyproject.container.scanner.DefaultClassPathScanner;
import io.fairyproject.container.scanner.ThreadedClassPathScanner;
import io.fairyproject.event.EventBus;
import io.fairyproject.event.impl.PostServiceInitialEvent;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.CompletableFutureUtils;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.thread.executor.ListeningDecorator;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.fairyproject.Debug.log;

public class ContainerContext {
    private static ContainerContext INSTANCE;
    public static boolean SINGLE_THREADED = Runtime.getRuntime().availableProcessors() < 2 || Boolean.getBoolean("fairy.singlethreaded");
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

    public static ContainerContext get() {
        return INSTANCE;
    }

    /**
     * The global node of containers
     */
    private ContainerNode node;

    public void init() {
        INSTANCE = this;

        this.node = ContainerNode.create("global");
        this.node.addObj(new SimpleContainerObj(this, this.getClass()));
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
            Debug.LOGGER.error("Error while scanning classes for framework", Stacktrace.simplifyStacktrace(throwable));
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

    public void stop() {
        try {
            this.node.closeAndReportException();
        } finally {
            INSTANCE = null;
        }
    }

    public @Nullable Object getContainerObject(@NonNull Class<?> type) {
        ContainerObj obj = ContainerRef.getObj(type);
        return obj == null ? null : obj.getInstance();
    }

    public boolean isRegisteredObject(Class<?>... types) {
        for (Class<?> type : types) {
            ContainerObj dependencyDetails = ContainerRef.getObj(type);
            if (dependencyDetails == null || dependencyDetails.getInstance() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isObject(Class<?> objectClass) {
        return ContainerRef.getObj(objectClass) != null;
    }

    public boolean isObject(Object object) {
        return this.isObject(object.getClass());
    }

    public Collection<ContainerObj> findDetailsBindWith(Plugin plugin) {
        return this.node.all()
                .stream()
                .filter(containerObject -> containerObject.isBind() && containerObject.getBindPlugin().equals(plugin))
                .collect(Collectors.toList());
    }

    /**
     * Registration
     */

    public ComponentContainerObj registerComponent(Object instance, String prefix, Class<?> type, ComponentHolder componentHolder) throws InvocationTargetException, IllegalAccessException {
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
                            Debug.LOGGER.error("Couldn't find the dependency " + dependency + " for " + type.getSimpleName() + "!");
                        case SUB_DISABLE:
                            return null;
                        case SUB:
                            break;
                    }
                }
            }
        }

        ComponentContainerObj containerObject = new ComponentContainerObj(type, instance, componentHolder);
        containerObject.lifeCycle(LifeCycle.CONSTRUCT);
        if (!containerObject.shouldActive()) {
            return null;
        }

        this.registerObject(containerObject);
        this.attemptBindPlugin(containerObject);

        try {
            containerObject.lifeCycle(LifeCycle.PRE_INIT);
        } catch (Throwable throwable) {
            Debug.LOGGER.error(throwable);
        }
        return containerObject;
    }

    public void attemptBindPlugin(ContainerObj containerObj) {
        if (PluginManager.isInitialized()) {
            Plugin plugin = PluginManager.INSTANCE.getPluginByClass(containerObj.getType());

            if (plugin != null) {
                containerObj.bindWith(plugin);

                log("ContainerObject " + containerObj.getType() + " is now bind with plugin " + plugin.getName());
            }
        }
    }

    public void lifeCycle(LifeCycle lifeCycle, Collection<ContainerObj> containerObjList) {
        this.lifeCycleAsynchronously(lifeCycle, containerObjList).join();
    }

    public CompletableFuture<?> lifeCycleAsynchronously(LifeCycle lifeCycle, Collection<ContainerObj> containerObjList) {
        List<CompletableFuture<?>> futures = new ArrayList<>(containerObjList.size());

        for (ContainerObj containerObj : containerObjList) {
            try {
                futures.add(containerObj.lifeCycle(lifeCycle));
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
