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

package io.fairyproject.container.processor.injection;

import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.processor.ContainerObjDestroyProcessor;
import io.fairyproject.container.processor.ContainerObjInitProcessor;
import io.fairyproject.event.Event;
import io.fairyproject.event.EventNode;
import io.fairyproject.event.EventSubscribeRegistry;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.metadata.MetadataKey;

public class SubscribeEventAnnotationProcessor implements ContainerObjInitProcessor, ContainerObjDestroyProcessor {

    private static final MetadataKey<EventNode> KEY = MetadataKey.create("fairy:event-node", EventNode.class);


    @Override
    public void processPostInitialization(ContainerObj object, Object instance) {
        if (!object.isSingletonScope())
            return;

        final EventNode<? extends Event> eventNode = EventSubscribeRegistry.create(instance);
        if (eventNode != null) {
            GlobalEventNode.get().addChild(eventNode);
            object.getMetadata().put(KEY, eventNode);
        }
    }

    @Override
    public void processPreDestroy(ContainerObj object, Object instance) {
        object.getMetadata().ifPresent(KEY, eventNode -> GlobalEventNode.get().removeChild(eventNode));
    }

}
