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

package io.fairyproject.container.object.resolver;

import io.fairyproject.container.ContainerConstruct;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.ContainerRef;
import io.fairyproject.util.AccessUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

@Getter
public class ConstructorContainerResolver extends ContainerResolverBase {

    private final Class<?> type;
    private final Constructor<?> constructor;

    @SneakyThrows
    public ConstructorContainerResolver(Class<?> type) {
        this.type = type;

        Constructor<?> constructorRet = null;
        int priorityRet = -1;

        for (Constructor<?> constructor : this.type.getDeclaredConstructors()) {
            AccessUtil.setAccessible(constructor);

            int priority = -1;
            ContainerConstruct annotation = constructor.getAnnotation(ContainerConstruct.class);
            if (annotation != null) {
                priority = annotation.priority();
            }

            if (constructorRet == null || priorityRet < priority) {
                constructorRet = constructor;
                priorityRet = priority;
            }
        }

        this.constructor = constructorRet;
        this.parameters = this.constructor.getParameters();
        for (Parameter parameter : this.parameters) {
            if (!ContainerRef.hasObj(parameter.getType())) {
                throw new IllegalArgumentException("The type " + parameter.getType().getName() + " it's not supposed to be in bean constructor!");
            }
        }
    }

    public Object newInstance(ContainerContext containerContext) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return this.constructor.newInstance(this.getParameters(containerContext));
    }

}
