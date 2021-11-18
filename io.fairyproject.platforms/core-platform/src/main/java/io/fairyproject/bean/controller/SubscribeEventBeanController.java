package io.fairyproject.bean.controller;

import io.fairyproject.bean.details.BeanDetails;
import io.fairyproject.event.EventBus;

public class SubscribeEventBeanController implements BeanController {
    @Override
    public void applyBean(BeanDetails beanDetails) {
        final Object instance = beanDetails.getInstance();
        if (instance != null)
            EventBus.subscribeAll(instance);
    }

    @Override
    public void removeBean(BeanDetails beanDetails) {
        final Object instance = beanDetails.getInstance();
        if (instance != null)
            EventBus.unsubscribeAll(instance);
    }
}
