package io.fairyproject.container.controller;

import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.event.EventNode;
import io.fairyproject.event.EventSubscribeRegistry;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.metadata.MetadataKey;

public class SubscribeEventContainerController implements ContainerController {

    private static final MetadataKey<EventNode> KEY = MetadataKey.create("fairy:event-node", EventNode.class);

    @Override
    public void applyContainerObject(ContainerObj containerObj) {
        final Object instance = containerObj.instance();
        if (instance == null)
            return;
        final EventNode<?> eventNode = EventSubscribeRegistry.create(instance);
        if (eventNode != null) {
            GlobalEventNode.get().addChild(eventNode);
            containerObj.metadata().put(KEY, eventNode);
        }
    }

    @Override
    public void removeContainerObject(ContainerObj containerObj) {
        containerObj.metadata().ifPresent(KEY, eventNode -> GlobalEventNode.get().removeChild(eventNode));
    }
}
