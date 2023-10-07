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

package io.fairyproject.container.processor.annotation;

import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.processor.ContainerObjDestroyProcessor;
import io.fairyproject.container.processor.ContainerObjInitProcessor;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LifeCycleAnnotationProcessor implements ContainerObjInitProcessor, ContainerObjDestroyProcessor {

    private static final int PRE_INIT = 0;
    private static final int POST_INIT = 1;
    private static final int PRE_DESTROY = 2;
    private static final int POST_DESTROY = 3;

    private final Class<? extends Annotation>[] annotations = new Class[4];
    private final Map<Class<?>, LifeCycleMetadata> metadataByType = new ConcurrentHashMap<>();

    public void setPreInitAnnotation(@Nullable Class<? extends Annotation> annotation) {
        this.annotations[PRE_INIT] = annotation;
    }

    public void setPostInitAnnotation(@Nullable Class<? extends Annotation> annotation) {
        this.annotations[POST_INIT] = annotation;
    }

    public void setPreDestroyAnnotation(@Nullable Class<? extends Annotation> annotation) {
        this.annotations[PRE_DESTROY] = annotation;
    }

    public void setPostDestroyAnnotation(@Nullable Class<? extends Annotation> annotation) {
        this.annotations[POST_DESTROY] = annotation;
    }

    private LifeCycleMetadata findOrCreateMetadata(Class<?> type) {
        return this.metadataByType.computeIfAbsent(type, t -> new LifeCycleMetadata(t, this.annotations));
    }

    @Override
    public void processPreInitialization(ContainerObj object, Object instance) {
        this.findOrCreateMetadata(instance.getClass()).invoke(PRE_INIT, instance);
    }

    @Override
    public void processPostInitialization(ContainerObj object, Object instance) {
        this.findOrCreateMetadata(instance.getClass()).invoke(POST_INIT, instance);
    }

    @Override
    public void processPreDestroy(ContainerObj object, Object instance) {
        this.findOrCreateMetadata(instance.getClass()).invoke(PRE_DESTROY, instance);
    }

    @Override
    public void processPostDestroy(ContainerObj object, Object instance) {
        this.findOrCreateMetadata(instance.getClass()).invoke(POST_DESTROY, instance);
    }

}
