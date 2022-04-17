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

package io.fairyproject.container.object;

import io.fairyproject.container.*;
import io.fairyproject.container.object.parameter.ContainerParameterConstructor;
import io.fairyproject.util.CompletableFutureUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public class ServiceContainerObject extends RelativeContainerObject {

    private ContainerParameterConstructor containerConstructor;

    public ServiceContainerObject(Class<?> type, Class<?>[] dependencies) {
        super(type, dependencies);

        this.loadAnnotations();
    }

    public void setupConstruction(ContainerContext containerContext) {
        this.containerConstructor = new ContainerParameterConstructor(this.getType());
        for (Parameter parameter : this.containerConstructor.getParameters()) {
            ContainerObject details = containerContext.getObjectDetails(parameter.getType());

            ServiceDependencyType type = ServiceDependencyType.FORCE;
            final DependencyType annotation = parameter.getAnnotation(DependencyType.class);
            if (annotation != null) {
                type = annotation.value();
            }
            this.addDependencies(type, details.getType());
        }
    }

    public CompletableFuture<?> build(ContainerContext context) {
        if (this.containerConstructor == null) {
            throw new IllegalArgumentException("The construction for ContainerObject " + this.getType().getName() + " hasn't been called!");
        }

        switch (this.getThreadingMode()) {
            case ASYNC:
                return super.build(context).thenAcceptAsync(ignored -> this.buildSync(context), ContainerContext.EXECUTOR);
            default:
            case SYNC:
                try {
                    this.buildSync(context);
                } catch (Throwable throwable) {
                    return CompletableFutureUtils.failureOf(throwable);
                }
                return super.build(context);
        }
    }

    private void buildSync(ContainerContext context) {
        try {
            this.setInstance(this.containerConstructor.newInstance(context));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadAnnotations(Collection<Class<?>> superClasses) {
        super.loadAnnotations(superClasses);

        for (Class<?> type : superClasses) {
            for (ServiceDependency dependency : type.getAnnotationsByType(ServiceDependency.class)) {
                this.addDependencies(dependency.type(), dependency.value());
            }
        }
    }
}
