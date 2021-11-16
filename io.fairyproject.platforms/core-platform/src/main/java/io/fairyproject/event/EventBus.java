package io.fairyproject.event;

import io.fairyproject.event.impl.AnnotatedSubscriber;
import io.fairyproject.util.Utility;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class EventBus {

    private final Logger LOGGER = LogManager.getLogger(EventBus.class);
    private final Map<Class<?>, Subscribers> GLOBAL_SUBSCRIBERS = new HashMap<>();

    static {
        GLOBAL_SUBSCRIBERS.put(Object.class, new Subscribers(Object.class));
    }

    public Subscribers getGlobalSubscribers() {
        return GLOBAL_SUBSCRIBERS.get(Object.class);
    }

    public void call(Object event) {
        Subscribers subscribers;
        if (!(event instanceof IEvent)) {
            subscribers = GLOBAL_SUBSCRIBERS.computeIfAbsent(event.getClass(), Subscribers::new);
        } else {
            subscribers = ((IEvent) event).getSubscribers();
        }
        for (Subscriber<?> subscriber : subscribers.all()) {
            try {
                subscriber.invoke0(event);
            } catch (Throwable throwable) {
                subscriber.handleException0(event, throwable);
            }
        }
    }

    public void subscribeAll(Object listener) {
        for (Class<?> type : Utility.getSuperAndInterfaces(listener.getClass())) {
            for (Method method : type.getDeclaredMethods()) {
                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                if (subscribe == null) {
                    continue;
                }

                if (method.getParameterCount() != 1) {
                    LOGGER.error("The method " + method + " is subscribing event but parameter count isn't 1");
                    continue;
                }

                final Class<?> parameter = method.getParameterTypes()[0];
                final AnnotatedSubscriber<?> subscriber = new AnnotatedSubscriber<>(parameter, subscribe.priority(), listener, method);
                EventBus.subscribe(subscriber);
            }
        }
    }

    public void subscribe(Subscriber<?> subscriber) {
        try {
            final Subscribers subscribers = getSubscribers(subscriber.getType());
            subscribers.register(subscriber);
        } catch (Throwable e) {
            LOGGER.error("An error occurs while subscribing event", e);
        }
    }

    public void unsubscribe(Subscriber<?> subscriber) {
        try {
            final Class<?> type = subscriber.getType();
            Subscribers subscribers = getSubscribers(type);
            subscribers.unregister(subscriber);

            subscribers = GLOBAL_SUBSCRIBERS.getOrDefault(type, null);
            if (subscribers != null && subscribers.isEmpty()) {
                GLOBAL_SUBSCRIBERS.remove(type);
            }
        } catch (Throwable e) {
            LOGGER.error("An error occurs while subscribing event", e);
        }
    }

    private Subscribers getSubscribers(Class<?> eventClass) throws IllegalAccessException {
        try {
            final Field field = eventClass.getDeclaredField("SUBSCRIBERS");
            field.setAccessible(true);

            return (Subscribers) field.get(null);
        } catch (NoSuchFieldException ex) {
            return GLOBAL_SUBSCRIBERS.computeIfAbsent(eventClass, c -> new Subscribers(eventClass));
        }
    }

}
