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

package io.fairyproject.bean.details.constructor;

import lombok.Getter;
import lombok.SneakyThrows;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.bean.BeanConstructor;
import io.fairyproject.bean.BeanContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

@Getter
public class BeanParameterDetailsConstructor extends BeanParameterDetailsAbstract {

    private final Class<?> type;
    private final Constructor<?> constructor;

    @SneakyThrows
    public BeanParameterDetailsConstructor(Class<?> type, BeanContext beanContext) {
        this.type = type;

        Constructor<?> constructorRet = null;
        int priorityRet = -1;

        for (Constructor<?> constructor : this.type.getDeclaredConstructors()) {
            AccessUtil.setAccessible(constructor);

            int priority = -1;
            BeanConstructor annotation = constructor.getAnnotation(BeanConstructor.class);
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
            if (!beanContext.isBean(parameter.getType())) {
                throw new IllegalArgumentException("The type " + parameter.getType().getName() + " it's not supposed to be in bean constructor!");
            }
        }
    }

    public Object newInstance(BeanContext beanContext) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return this.constructor.newInstance(this.getParameters(beanContext));
    }

}
