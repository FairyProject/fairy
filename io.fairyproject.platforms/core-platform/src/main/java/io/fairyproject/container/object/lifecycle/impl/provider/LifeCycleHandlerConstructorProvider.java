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
import io.fairyproject.container.object.resolver.ConstructorContainerResolver;
import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.exceptionally.ThrowingRunnable;

import java.util.concurrent.CompletableFuture;

public class LifeCycleHandlerConstructorProvider implements LifeCycleHandler {

    private final ContainerObj obj;
    private ConstructorContainerResolver constructor;

    public LifeCycleHandlerConstructorProvider(ContainerObj obj) {
        this.obj = obj;
    }

    @Override
    public void init() {
        if (this.obj.instance() != null)
            return;
        this.constructor = new ConstructorContainerResolver(obj.type());
        for (Class<?> type : this.constructor.getTypes()) {
            this.obj.addDepend(type, ServiceDependencyType.FORCE);
        }
    }

    @Override
    public CompletableFuture<?> onConstruct() {
        if (this.obj.instance() != null)
            return AsyncUtils.empty();

        return this.obj.threadingMode().execute(ThrowingRunnable.sneaky(() -> {
            Object instance = this.constructor.newInstance(ContainerContext.get());
            this.obj.setInstance(instance);
        }));
    }
}
