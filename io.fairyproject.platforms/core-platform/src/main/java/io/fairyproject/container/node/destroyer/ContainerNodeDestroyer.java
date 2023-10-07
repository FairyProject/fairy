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

package io.fairyproject.container.node.destroyer;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.processor.ContainerObjDestroyProcessor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContainerNodeDestroyer {

    private final ContainerContext context;

    public void destroy(ContainerNode node) {
        node.graph().forEachCounterClockwise(this::handlePreDestroy);
        node.graph().forEachCounterClockwise(this::handlePostDestroy);

        for (ContainerNode child : node.childs()) {
            this.context.nodeDestroyer().destroy(child);
        }
    }

    private void handlePreDestroy(ContainerObj object) {
        this.callPreDestroyProcessor(object);
    }

    private void callPreDestroyProcessor(ContainerObj object) {
        if (!object.isSingletonScope())
            return;

        Object instance = context.singletonObjectRegistry().getSingleton(object.getType());
        if (instance == null)
            return;

        for (ContainerObjDestroyProcessor destroyProcessor : this.context.destroyProcessors()) {
            destroyProcessor.processPreDestroy(object, instance);
        }
    }

    private void handlePostDestroy(ContainerObj object) {
        this.callPostDestroyProcessor(object);

        // completely destroy the object
        Class<?> type = object.getType();

        this.context.containerObjectBinder().unbind(type);
        this.context.objectCollectorRegistry().removeFromCollectors(object);
        this.context.singletonObjectRegistry().removeSingleton(type);
    }

    private void callPostDestroyProcessor(ContainerObj object) {
        if (!object.isSingletonScope())
            return;

        Object instance = context.singletonObjectRegistry().getSingleton(object.getType());
        if (instance == null)
            return;

        for (ContainerObjDestroyProcessor destroyProcessor : this.context.destroyProcessors()) {
            destroyProcessor.processPostDestroy(object, instance);
        }
    }

}
