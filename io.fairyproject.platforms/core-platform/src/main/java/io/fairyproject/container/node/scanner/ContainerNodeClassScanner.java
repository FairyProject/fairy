/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.container.node.scanner;

import io.fairyproject.Debug;
import io.fairyproject.container.DependsOn;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.binder.ContainerObjectBinder;
import io.fairyproject.container.binder.ContainerObjectBinderImpl;
import io.fairyproject.container.configuration.Configuration;
import io.fairyproject.container.configuration.TestConfiguration;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.provider.ConstructorInstanceProvider;
import io.fairyproject.container.object.provider.InstanceProvider;
import io.fairyproject.container.processor.ContainerNodeClassScanProcessor;
import io.fairyproject.container.processor.ContainerProcessors;
import io.fairyproject.log.Log;
import io.fairyproject.util.Utility;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Getter
public class ContainerNodeClassScanner {

    private final ContainerProcessors processors;
    private final ContainerObjectBinder binder;
    private final String name;
    private final ContainerNode node;
    private final List<String> classPaths = new ArrayList<>();
    private final List<String> excludedClassPaths = new ArrayList<>();
    private final List<URL> urls = new ArrayList<>();
    private final List<ClassLoader> classLoaders = new ArrayList<>();

    private ContainerNode objNode;

    public void scan() {
        this.objNode = ContainerNode.create(this.name + ":obj", this.binder);
        this.node.addChild(this.objNode);

        final ClassGraph classGraph = createClassGraph();

        try (ScanResult scanResult = classGraph.scan(4)) {
            this.loadComponentClasses(scanResult);
            this.loadComponentConfigurations(scanResult, Configuration.class, false);
            if (Debug.UNIT_TEST)
                this.loadComponentConfigurations(scanResult, TestConfiguration.class, true);

            this.loadLegacyComponentClasses(scanResult);
            this.callProcessors(scanResult);
        }
    }

    private void callProcessors(ScanResult scanResult) {
        for (ContainerNodeClassScanProcessor nodeClassScanProcessor : this.processors.nodeClassScanProcessors()) {
            nodeClassScanProcessor.processClassScan(this.node, scanResult);
        }
    }

    private void loadLegacyComponentClasses(ScanResult scanResult) {
        new ContainerNodeLegacyScanner(scanResult, this.binder, this).load();
    }


    private void loadComponentConfigurations(ScanResult scanResult, Class<? extends Annotation> annotation, boolean override) {
        ClassInfoList classes = scanResult.getClassesWithAnnotation(annotation);

        for (ClassInfo classInfo : classes) {
            Class<?> javaClass;
            try {
                javaClass = classInfo.loadClass();
            } catch (Throwable t) {
                Log.error(t);
                continue;
            }

            this.loadConfigurationClass(javaClass, override);
        }
    }

    private void loadConfigurationClass(Class<?> javaClass, boolean override) {
        new ContainerNodeConfigurationScanner(this.binder, javaClass, override, this).load();
    }

    private void loadComponentClasses(ScanResult scanResult) {
        ClassInfoList classes = scanResult.getClassesWithAnnotation(InjectableComponent.class);

        loadComponentClasses(classes, this.node);
    }

    void loadComponentClasses(ClassInfoList classes, ContainerNode node) {
        loadComponentClasses(classes, node, obj -> {});
    }

    void loadComponentClasses(ClassInfoList classes, ContainerNode node, Consumer<ContainerObj> closure) {
        for (ClassInfo classInfo : classes) {
            Class<?> javaClass;
            try {
                javaClass = classInfo.loadClass();
            } catch (Throwable t) {
                Log.error(t);
                continue;
            }

            ContainerObj object = this.getOrLoadComponentObject(javaClass);

           this.addComponentClass(object, node, closure);
        }
    }

    private ContainerObj getOrLoadComponentObject(Class<?> javaClass) {
        ContainerObj binding = this.binder.getBinding(javaClass);
        if (binding != null)
            return binding;

        ContainerObj object = this.createObject(javaClass);
        InjectableComponent annotation = object.getType().getAnnotation(InjectableComponent.class);
        if (annotation != null) {
            object.setScope(annotation.scope());
        }

        try {
            InstanceProvider provider = new ConstructorInstanceProvider(javaClass);

            object.setInstanceProvider(provider);
        } catch (Throwable t) {
            throw new RuntimeException("An error occurred while creating the component instance provider", t);
        }

        this.processDependsOn(object);

        return object;
    }

    private void processDependsOn(ContainerObj object) {
        Class<?> type = object.getType();

        for (Class<?> superClass : Utility.getSuperAndInterfaces(type)) {
            DependsOn annotation = superClass.getDeclaredAnnotation(DependsOn.class);
            if (annotation == null)
                continue;

            for (Class<?> dependency : annotation.value()) {
                object.addDependency(dependency);
            }
        }
    }

    ContainerObj createObject(Class<?> javaClass) {
        ContainerObj object = ContainerObj.create(javaClass);
        this.binder.bind(javaClass, object);

        return object;
    }

    void addComponentClass(ContainerObj object, ContainerNode node) {
        addComponentClass(object, node, $ -> {});
    }

    void addComponentClass(ContainerObj object, ContainerNode node, Consumer<ContainerObj> closure) {
        node.addObj(object);
    }

    private ClassGraph createClassGraph() {
        final ClassGraph classGraph = new ClassGraph()
                .enableAllInfo()
                .verbose(false);

        for (String classPath : classPaths)
            classGraph.acceptPackages(classPath);

        for (String classPath : this.excludedClassPaths)
            classGraph.rejectPackages(classPath);

        if (!urls.isEmpty())
            classGraph.overrideClasspath(urls);

        if (!classLoaders.isEmpty())
            classGraph.overrideClassLoaders(classLoaders.toArray(new ClassLoader[0]));

        return classGraph;
    }

}
