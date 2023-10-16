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

import io.fairyproject.Fairy;
import io.fairyproject.container.binder.ContainerObjectBinder;
import io.fairyproject.container.binder.ContainerObjectBinderImpl;
import io.fairyproject.container.collection.ContainerObjCollectorRegistry;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.destroyer.ContainerNodeDestroyer;
import io.fairyproject.container.object.singleton.SingletonObjectRegistry;
import io.fairyproject.container.processor.*;
import io.fairyproject.container.processor.annotation.FairyLifeCycleAnnotationProcessor;
import io.fairyproject.container.processor.injection.AutowiredAnnotationProcessor;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.event.impl.PostServiceInitialEvent;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.thread.NamedThreadFactory;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.fairyproject.Debug.log;

@Getter
@Accessors(fluent = true)
public class ContainerContext implements ContainerProcessors {
    @Deprecated
    private static ContainerContext INSTANCE;
    public static final int PLUGIN_LISTENER_PRIORITY = 100;

    public final ExecutorService executor;
    private final ContainerObjCollectorRegistry objectCollectorRegistry;
    private final ContainerObjectBinder containerObjectBinder;
    private final SingletonObjectRegistry singletonObjectRegistry;
    private final ContainerNodeDestroyer nodeDestroyer;
    private final ContainerObjConstructProcessor[] constructProcessors;
    private final ContainerObjInitProcessor[] initProcessors;
    private final ContainerObjDestroyProcessor[] destroyProcessors;
    private final ContainerNodeClassScanProcessor[] nodeClassScanProcessors;
    private final ContainerNodeInitProcessor[] nodeInitProcessors;
    private ContainerNode node;

    public ContainerContext() {
        this.executor = Executors.newCachedThreadPool(NamedThreadFactory.builder()
                .name("Container Thread - %d")
                .daemon(true)
                .uncaughtExceptionHandler((thread, throwable) -> Log.error("Exception occurred in Container Thread", throwable))
                .build());
        this.containerObjectBinder = new ContainerObjectBinderImpl();
        this.objectCollectorRegistry = new ContainerObjCollectorRegistry();
        this.singletonObjectRegistry = SingletonObjectRegistry.create();
        this.nodeDestroyer = new ContainerNodeDestroyer(this);

        FairyLifeCycleAnnotationProcessor annotationProcessor = new FairyLifeCycleAnnotationProcessor();
        AutowiredAnnotationProcessor autowiredAnnotationProcessor = new AutowiredAnnotationProcessor();
        this.constructProcessors = new ContainerObjConstructProcessor[] { };
        this.initProcessors = new ContainerObjInitProcessor[] { autowiredAnnotationProcessor, annotationProcessor };
        this.destroyProcessors = new ContainerObjDestroyProcessor[] { annotationProcessor };
        this.nodeClassScanProcessors = new ContainerNodeClassScanProcessor[] { autowiredAnnotationProcessor };
        this.nodeInitProcessors = new ContainerNodeInitProcessor[] { autowiredAnnotationProcessor };
    }

    public void init() {
        INSTANCE = this;

        this.node = new RootNodeLoader(this).load();

        if (PluginManager.isInitialized()) {
            log("Find PluginManager, attempt to register Plugin Listeners");
            PluginManager.INSTANCE.registerListener(new ContainerNodePluginListener(this));
        }

        Fairy.getPlatform().onPostServicesInitial();
        GlobalEventNode.get().call(new PostServiceInitialEvent());
    }

    public void stop() {
        try {
            this.nodeDestroyer.destroy(this.node);
        } finally {
            INSTANCE = null;
        }
    }

    public boolean isObject(Class<?> objectClass) {
        return this.containerObjectBinder.isBound(objectClass);
    }

    public boolean isObject(Object object) {
        return this.isObject(object.getClass());
    }

    public Object getSingleton(Class<?> objectClass) {
        return this.singletonObjectRegistry.getSingleton(objectClass);
    }

    public List<String> findClassPaths(Class<?> plugin) {
        ClasspathScan annotation = plugin.getAnnotation(ClasspathScan.class);

        if (annotation != null) {
            return Arrays.asList(annotation.value());
        }

        return Collections.emptyList();
    }

    @Deprecated
    public static ContainerContext get() {
        return INSTANCE;
    }

}
