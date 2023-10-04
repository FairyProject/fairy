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

package io.fairyproject.container.object.provider;

import io.fairyproject.container.ContainerConstruct;
import io.fairyproject.util.AccessUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class ConstructorInstanceProvider implements InstanceProvider {

    private final Class<?> type;
    private final Constructor<?> constructor;

    public ConstructorInstanceProvider(Class<?> type) throws ReflectiveOperationException {
        if (Modifier.isAbstract(type.getModifiers()))
            throw new IllegalArgumentException("The type " + type.getName() + " is abstract!");

        this.type = type;
        this.constructor = this.resolveConstructor();
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }

    @Override
    public Class<?>[] getDependencies() {
        return constructor.getParameterTypes();
    }

    @Override
    public @NotNull Object provide(Object[] objects) throws Exception {
        return this.constructor.newInstance(objects);
    }

    private Constructor<?> resolveConstructor() throws ReflectiveOperationException {
        Constructor<?> bestConstructor = null;
        int bestPriority = -1;

        for (Constructor<?> constructor : this.type.getDeclaredConstructors()) {
            AccessUtil.setAccessible(constructor);

            int priority = -1;
            ContainerConstruct annotation = constructor.getAnnotation(ContainerConstruct.class);
            if (annotation != null)
                priority = annotation.priority();

            if (bestConstructor == null || bestPriority < priority) {
                bestConstructor = constructor;
                bestPriority = priority;
            }
        }

        if (bestConstructor == null)
            bestConstructor = this.type.getDeclaredConstructor();

        return bestConstructor;
    }

}
