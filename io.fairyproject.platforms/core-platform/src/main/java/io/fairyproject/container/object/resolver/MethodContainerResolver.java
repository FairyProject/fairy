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

import lombok.Getter;
import lombok.SneakyThrows;
import io.fairyproject.container.ContainerContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Getter
public class MethodContainerResolver extends ContainerResolverBase {

    private final Method method;

    @SneakyThrows
    public MethodContainerResolver(Method method, ContainerContext containerContext) {
        this.method = method;

        this.types = this.method.getParameterTypes();
        for (Class<?> type : this.types) {
            if (!containerContext.isObject(type)) {
                throw new IllegalArgumentException("The type " + type.getName() + " is not a bean!, it's not supposed to be in bean method!");
            }
        }
    }

    public String name() {
        return this.method.getName();
    }

    public Class<?> returnType() {
        return this.method.getReturnType();
    }

    public Object invoke(Object instance, ContainerContext containerContext) throws InvocationTargetException, IllegalAccessException {
        Object[] parameters = this.resolve(containerContext);

        return this.method.invoke(instance, parameters);
    }

}
