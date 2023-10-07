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

import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.Utility;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LifeCycleMetadata {

    private final Class<? extends Annotation>[] annotations;
    private final List<LifeCycleElement>[] processors;

    public LifeCycleMetadata(Class<?> instanceClass, Class<? extends Annotation>[] annotations) {
        this.annotations = annotations;
        this.processors = new List[4];
        for (int i = 0; i < this.processors.length; i++) {
            this.processors[i] = new ArrayList<>();
        }

        for (Class<?> superClass : Utility.getSuperClasses(instanceClass)) {
            try {
                for (Method declaredMethod : superClass.getDeclaredMethods()) {
                    this.handleMethodInit(declaredMethod);
                }
            } catch (Throwable throwable) {
                throw new IllegalStateException("An error occurs while scanning class " + superClass, throwable);
            }
        }
    }

    private void handleMethodInit(Method method) {
        for (int index = 0; index < this.annotations.length; index++) {
            final Class<? extends Annotation> annotation = this.annotations[index];
            this.handleAnnotationInit(method, index, annotation);
        }
    }

    private void handleAnnotationInit(Method method, int index, @Nullable Class<? extends Annotation> annotation) {
        // The annotation is not handled in this processor
        if (annotation == null)
            return;

        // The method wasn't annotated by target annotation
        if (!method.isAnnotationPresent(annotation))
            return;

        // The method has extra parameter which is not allowed
        ConditionUtils.is(method.getParameterCount() == 0, String.format("The method %s annotated with %s is not supposed to have parameters", method, annotation));

        LifeCycleElement processor = new LifeCycleElement(method);
        this.processors[index].add(processor);
    }

    public void invoke(int index, Object instance) {
        ConditionUtils.notNull(instance, "Instance of the container object hasn't yet been constructed.");

        this.processors[index].forEach(processor -> processor.invoke(instance));
    }

}
