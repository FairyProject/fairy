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

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.ContainerReference;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.util.ConditionUtils;
import lombok.Getter;

public class ContainerResolverBase implements ContainerResolver {

    @Getter
    protected Class<?>[] types;

    @Override
    public Object[] resolve(ContainerContext containerContext) {
        if (this.types == null)
            throw new IllegalArgumentException("No parameters found!");
        Object[] args = new Object[this.types.length];

        for (int i = 0; i < args.length; i++) {
            ContainerObj obj = ContainerReference.getObj(this.types[i]);
            ConditionUtils.notNull(obj, String.format("Couldn't find container object %s!", this.types[i].getName()));
            ConditionUtils.notNull(obj.instance(), String.format("Container obj %s has no instance!", obj.type()));

            args[i] = obj.instance();
        }

        return args;
    }
}
