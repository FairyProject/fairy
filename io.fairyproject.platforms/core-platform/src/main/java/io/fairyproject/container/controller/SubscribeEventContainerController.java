package io.fairyproject.container.controller;

import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.event.EventBus;

public class SubscribeEventContainerController implements ContainerController {
    @Override
    public void applyBean(ContainerObject containerObject) {
        final Object instance = containerObject.getInstance();
        if (instance != null)
            EventBus.subscribeAll(instance);
    }

    @Override
    public void removeBean(ContainerObject containerObject) {
        final Object instance = containerObject.getInstance();
        if (instance != null)
            EventBus.unsubscribeAll(instance);
    }
}
