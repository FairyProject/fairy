package io.fairyproject.event.impl;

import io.fairyproject.event.Subscriber;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

public class AnnotatedSubscriber<E> extends Subscriber<E> {

    // TODO: made asm generated method instead of reflection or method handles

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AnnotatedSubscriber<?> that = (AnnotatedSubscriber<?>) o;
        return listener.equals(that.listener) && method.equals(that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), listener, method);
    }
}
