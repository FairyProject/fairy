package io.fairyproject.event.impl;

import io.fairyproject.event.Subscriber;

import java.util.function.Consumer;

public class ConsumerSubscriber<E> extends Subscriber<E> {

    private final Consumer<E> consumer;

    public ConsumerSubscriber(Class<E> type, int priority, Consumer<E> consumer) {
        super(type, priority);
        this.consumer = consumer;
    }

    @Override
    public void invoke(E event) throws Throwable {
        this.consumer.accept(event);
    }

}
