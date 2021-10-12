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

package org.fairy.cache.impl;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class CacheKeyMethod extends CacheKeyAbstract {

    private final Method method;
    private final Object[] arguments;

    public CacheKeyMethod(Class<?> parentClass, final JoinPoint point) {
        super(parentClass);
        this.method = ((MethodSignature) point.getSignature()).getMethod();
        this.arguments = point.getArgs();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.method);
    }

    @Override
    public final boolean equals(Object object) {
        boolean equals;
        if (this == object) {
            equals = true;
        } else if (object instanceof CacheKeyMethod) {
            CacheKeyMethod key = (CacheKeyMethod) object;
            equals = key.method.equals(this.method)
                    && Arrays.deepEquals(key.arguments, this.arguments);
        } else {
            equals = false;
        }
        return equals && super.equals(object);
    }

}
