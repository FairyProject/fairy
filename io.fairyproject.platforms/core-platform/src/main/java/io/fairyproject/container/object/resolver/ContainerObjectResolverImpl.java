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

import io.fairyproject.container.binder.ContainerObjectBinder;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.util.ConditionUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class ContainerObjectResolverImpl implements ContainerObjectResolver {

    private final ContainerObjectBinder binder;
    private final ContainerObjectFactory singletonObjectFactory;
    private final ContainerObjectFactory prototypeObjectFactory;

    @Override
    public CompletableFuture<Object[]> resolveInstances(@NotNull Class<?>[] types) throws Exception {
        Object[] args = new Object[types.length];
        CompletableFuture<?>[] futures = new CompletableFuture<?>[types.length];

        for (int i = 0; i < args.length; i++) {
            Class<?> type = types[i];
            int index = i;

            futures[i] = this.resolveInstance(type).thenAccept(instance -> args[index] = instance);
        }

        return CompletableFuture.allOf(futures).thenApply($ -> args);
    }

    @Override
    public CompletableFuture<Object> resolveInstance(@NotNull Class<?> type) throws Exception {
        ContainerObj object = this.binder.getBinding(type);
        ConditionUtils.notNull(object, String.format("Couldn't find container object %s!", type.getName()));

        CompletableFuture<Object> future = null;
        switch (object.getScope()) {
            case SINGLETON:
                future = singletonObjectFactory.createInstance(object.getType());
                break;
            case PROTOTYPE:
                future = prototypeObjectFactory.createInstance(object.getType());
                break;
        }

        return future;
    }
}
