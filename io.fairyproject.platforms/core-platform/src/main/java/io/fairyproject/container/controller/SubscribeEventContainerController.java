package io.fairyproject.container.controller;

import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.event.EventBus;

public class SubscribeEventContainerController implements ContainerController {
    @Override
    public void applyContainerObject(ContainerObj containerObj) {
        final Object instance = containerObj.instance();
        if (instance != null)
            EventBus.subscribeAll(instance);
    }

    @Override
    public void removeContainerObject(ContainerObj containerObj) {
        final Object instance = containerObj.instance();
        if (instance != null)
            EventBus.unsubscribeAll(instance);
    }
}
