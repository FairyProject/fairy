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

package io.fairyproject.container.object.lifecycle.impl.provider;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.lifecycle.LifeCycleHandler;
import io.fairyproject.container.object.resolver.MethodContainerResolver;
import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class LifeCycleHandlerMethodProvider implements LifeCycleHandler {

    private final ContainerContext context;
    private final ContainerObj containerObj;
    private final Method method;
    private MethodContainerResolver methodContainerResolver;

    @Override
    public void init() {
        this.methodContainerResolver = new MethodContainerResolver(method, context);

        for (Class<?> type : this.methodContainerResolver.getTypes())
            this.containerObj.addDepend(type, ServiceDependencyType.FORCE);
    }

    @Override
    public CompletableFuture<?> onConstruct() {
        if (containerObj.instance() != null)
            return AsyncUtils.empty();

        return this.containerObj.threadingMode().execute(() -> {
            Object instance;
            try {
                instance = this.methodContainerResolver.invoke(null, context);
            } catch (InvocationTargetException | IllegalAccessException ex) {
                SneakyThrowUtil.sneakyThrow(ex);
                return;
            }

            this.containerObj.setInstance(instance);
        });
    }
}
