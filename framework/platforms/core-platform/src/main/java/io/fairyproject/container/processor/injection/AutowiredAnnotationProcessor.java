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

package io.fairyproject.container.processor.injection;

import io.fairyproject.container.Autowired;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.resolver.ContainerObjectResolver;
import io.fairyproject.container.processor.ContainerNodeClassScanProcessor;
import io.fairyproject.container.processor.ContainerNodeInitProcessor;
import io.fairyproject.container.processor.ContainerObjConstructProcessor;
import io.fairyproject.container.processor.ContainerObjInitProcessor;
import io.fairyproject.log.Log;
import io.fairyproject.reflect.Reflect;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.ClassGraphUtil;
import io.fairyproject.util.Utility;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AutowiredAnnotationProcessor implements
        ContainerObjInitProcessor,
        ContainerNodeClassScanProcessor,
        ContainerNodeInitProcessor {

    private final Map<String, NodeContext> nodes = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<?> processPreInitialization(ContainerObj object, Object instance, ContainerObjectResolver resolver) {
        List<Field> fields = new ArrayList<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // make sure we get all fields from superclasses
        for (Class<?> superClass : Utility.getSuperClasses(instance.getClass())) {
            fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
        }

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            Autowired annotation = field.getAnnotation(Autowired.class);
            if (annotation == null || Modifier.isStatic(modifiers))
                continue;

            if (Modifier.isFinal(modifiers))
                throw new IllegalStateException("The field " + field + " is final but marked @Autowired");

            try {
                futures.add(this.injectAutowiredField(field, instance, resolver));
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to inject field " + field, ex);
            }
        }

        return AsyncUtils.allOf(futures);
    }



    @Override
    public void processClassScan(ContainerNode node, ScanResult scanResult) {
        NodeContext context = this.nodes.computeIfAbsent(node.name(), key -> new NodeContext());

        ClassGraphUtil.fieldWithAnnotation(scanResult, Autowired.class)
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .forEach(context::addStaticField);
    }

    @Override
    public void processNodePreInitialization(ContainerNode node, ContainerObjectResolver containerObjectResolver) {
        NodeContext context = this.nodes.get(node.name());
        if (context != null) {
            context.injectStaticFields(containerObjectResolver);
        }
    }

    public CompletableFuture<?> injectAutowiredField(Field field, Object fieldInstance, ContainerObjectResolver resolver) throws Exception {
        Class<?> type = field.getType();
        CompletableFuture<Object> future = resolver.resolveInstance(type);

        return future.thenAccept(instance -> {
            try {
                AccessUtil.setAccessible(field);
                Reflect.setField(fieldInstance, field, instance);
            } catch (Exception ex) {
                Log.error("Failed to set field %s", field, ex);
            }
        });
    }

    private class NodeContext {
        private final List<Field> staticFields = new ArrayList<>();

        public void addStaticField(Field field) {
            this.staticFields.add(field);
        }

        public void injectStaticFields(ContainerObjectResolver containerObjectResolver) {
            for (Field field : this.staticFields) {
                try {
                    AutowiredAnnotationProcessor.this.injectAutowiredField(field, null, containerObjectResolver);
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to inject static field " + field, ex);
                }
            }
        }

    }

}
