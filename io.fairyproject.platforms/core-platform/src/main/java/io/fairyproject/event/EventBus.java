package io.fairyproject.event;

import io.fairyproject.event.impl.AnnotatedSubscriber;
import io.fairyproject.event.impl.ConsumerSubscriber;
import io.fairyproject.log.Log;
import io.fairyproject.util.Utility;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@UtilityClass
public class EventBus {
    private final Map<Class<?>, Subscribers> SUBSCRIBERS = new ConcurrentHashMap<>();

    static {
        SUBSCRIBERS.put(Object.class, new Subscribers(Object.class));
    }

    public Subscribers getGlobalSubscribers() {
        return SUBSCRIBERS.get(Object.class);
    }

    public void shutdown() {
        SUBSCRIBERS.forEach((k, v) -> v.clear());
        SUBSCRIBERS.clear();
    }

    public void call(Object event) {
//        Subscribers subscribers;
//        if (!(event instanceof Event)) {
//            subscribers = SUBSCRIBERS.get(event.getClass());
//        } else {
//            subscribers = ((Event) event).getSubscribers();
//        }
//        if (subscribers == null) {
//            return;
//        }
//        for (Subscriber<?> subscriber : subscribers.all()) {
//            try {
//                subscriber.invoke0(event);
//            } catch (Throwable throwable) {
//                subscriber.handleException0(event, throwable);
//            }
//        }
    }

    public void subscribeAll(Object listener) {
//        for (Class<?> type : Utility.getSuperAndInterfaces(listener.getClass())) {
//            for (Method method : type.getDeclaredMethods()) {
//                Subscribe subscribe = method.getAnnotation(Subscribe.class);
//                if (subscribe == null) {
//                    continue;
//                }
//
//                if (method.getParameterCount() != 1) {
//                    LOGGER.error("The method " + method + " is subscribing event but parameter count isn't 1");
//                    continue;
//                }
//
//                final Class<?> parameter = method.getParameterTypes()[0];
//                final AnnotatedSubscriber<?> subscriber = new AnnotatedSubscriber<>(parameter, subscribe.priority(), listener, method);
//                EventBus.subscribe(subscriber);
//            }
//        }
    }

    public void unsubscribeAll(Object listener) {
        SUBSCRIBERS.values().forEach(subscribers -> subscribers.unregisterObject(listener));
    }

    public <E> void subscribe(Class<E> eventClass, Consumer<E> consumer) {
        EventBus.subscribe(eventClass, 0, consumer);
    }

    public <E> void subscribe(Class<E> eventClass, int priority, Consumer<E> consumer) {
        EventBus.subscribe(new ConsumerSubscriber<>(eventClass, priority, consumer));
    }

    public void subscribe(Subscriber<?> subscriber) {
        try {
            final Subscribers subscribers = getSubscribers(subscriber.getType());
            subscribers.register(subscriber);
        } catch (Throwable e) {
            Log.error("An error occurs while subscribing event", e);
        }
    }

    public void unsubscribe(Subscriber<?> subscriber) {
        try {
            final Class<?> type = subscriber.getType();
            Subscribers subscribers = getSubscribers(type);
            subscribers.unregister(subscriber);

            subscribers = SUBSCRIBERS.getOrDefault(type, null);
            if (subscribers != null && subscribers.isEmpty()) {
                SUBSCRIBERS.remove(type);
            }
        } catch (Throwable e) {
            Log.error("An error occurs while subscribing event", e);
        }
    }

    private Subscribers getSubscribers(Class<?> eventClass) {
        return SUBSCRIBERS.computeIfAbsent(eventClass, c -> {
            try {
                final Field field = eventClass.getDeclaredField("SUBSCRIBERS");
                field.setAccessible(true);

                return (Subscribers) field.get(null);
            } catch (NoSuchFieldException ex) {
                return new Subscribers(eventClass);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

}
