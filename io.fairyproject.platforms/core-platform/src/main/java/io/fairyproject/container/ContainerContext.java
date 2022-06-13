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
import io.fairyproject.FairyPlatform;
import io.fairyproject.container.collection.ContainerObjCollectorRegistry;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.controller.SubscribeEventContainerController;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.ContainerNodeScanner;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.lifecycle.LifeCycleHandlerRegistry;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.event.impl.PostServiceInitialEvent;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.thread.executor.ListeningDecorator;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import static io.fairyproject.Debug.log;

@Accessors(fluent = true)
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
    @Getter
    private ContainerNode node;

    @Getter
    private LifeCycleHandlerRegistry lifeCycleHandlerRegistry;

    @Getter
    private ContainerObjCollectorRegistry objectCollectorRegistry;

    public void init() {
        INSTANCE = this;

        this.lifeCycleHandlerRegistry = new LifeCycleHandlerRegistry();
        this.objectCollectorRegistry = new ContainerObjCollectorRegistry();

        this.node = ContainerNode.create("global");
        this.node.addObj(ContainerObj.of(this.getClass(), this));
        this.node.addObj(ContainerObj.of(FairyPlatform.class, Fairy.getPlatform()));
        log("ContainerContext has been registered as ContainerObject.");

        try {
            ContainerNodeScanner scanner = new ContainerNodeScanner();
            scanner.name("framework");
            scanner.node(this.node);
            scanner.classPath(Fairy.getFairyPackage());
            if (!Debug.UNIT_TEST) {
                scanner.url(this.getClass().getProtectionDomain().getCodeSource().getLocation());
                scanner.classLoader(ContainerContext.class.getClassLoader());
            }

            scanner.scan();
        } catch (Throwable throwable) {
            Log.error("Error while scanning classes for framework", Stacktrace.simplifyStacktrace(throwable));
            Fairy.getPlatform().shutdown();
            return;
        }

        if (PluginManager.isInitialized()) {
            log("Find PluginManager, attempt to register Plugin Listeners");
            PluginManager.INSTANCE.registerListener(new ContainerPluginListener(this));
        }

        Fairy.getPlatform().onPostServicesInitial();
        GlobalEventNode.get().call(new PostServiceInitialEvent());
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
        return obj == null ? null : obj.instance();
    }

    public boolean isRegisteredObject(Class<?>... types) {
        for (Class<?> type : types) {
            ContainerObj dependencyDetails = ContainerRef.getObj(type);
            if (dependencyDetails == null || dependencyDetails.instance() == null) {
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

    public List<String> findClassPaths(Class<?> plugin) {
        ClasspathScan annotation = plugin.getAnnotation(ClasspathScan.class);

        if (annotation != null) {
            return Lists.newArrayList(annotation.value());
        }

        return Lists.newArrayList();
    }

    public ContainerNodeScanner scanClasses() {
        return new ContainerNodeScanner();
    }

}
