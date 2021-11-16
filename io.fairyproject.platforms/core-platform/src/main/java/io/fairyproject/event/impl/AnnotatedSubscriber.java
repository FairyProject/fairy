package io.fairyproject.event.impl;

import io.fairyproject.event.Subscriber;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class AnnotatedSubscriber<E> extends Subscriber<E> {

    private final Object listener;
    private final MethodHandle method;

    public AnnotatedSubscriber(Class<E> type, int priority, Object listener, Method method) {
        super(type, priority);
        this.listener = listener;
        try {
            method.setAccessible(true);
            this.method = MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInstance(Object obj) {
        return obj == this.listener;
    }

    @Override
    public void invoke(E event) throws Throwable {
        this.method.invoke(this.listener, event);
    }
}
