package io.fairyproject.event.impl;

import io.fairyproject.event.EventBus;
import io.fairyproject.event.Subscribers;

import java.lang.reflect.Field;

public class SecondaryEvent extends TestEvent {

    public static final Subscribers SUBSCRIBERS;

    static {
        Subscribers child = null;
        for (Class<?> type = SecondaryEvent.class.getSuperclass(); type != null; type = type.getSuperclass()) {
            try {
                final Field subscribers = type.getDeclaredField("SUBSCRIBERS");
                subscribers.setAccessible(true);

                child = (Subscribers) subscribers.get(null);
                break;
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (child == null) {
            child = EventBus.getGlobalSubscribers();
        }

        SUBSCRIBERS = new Subscribers(SecondaryEvent.class, child);
    }

    @Override
    public Subscribers getSubscribers() {
        return SUBSCRIBERS;
    }

}
