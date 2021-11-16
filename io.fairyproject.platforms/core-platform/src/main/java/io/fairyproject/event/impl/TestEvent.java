package io.fairyproject.event.impl;

import io.fairyproject.event.EventBus;
import io.fairyproject.event.IEvent;
import io.fairyproject.event.Subscribers;

public class TestEvent implements IEvent {

    public static final Subscribers SUBSCRIBERS = new Subscribers(TestEvent.class, EventBus.getGlobalSubscribers());

    @Override
    public Subscribers getSubscribers() {
        return SUBSCRIBERS;
    }
}
