package io.fairyproject.event;

import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.Utility;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

@UtilityClass
public class EventSubscribeRegistry {

    public @Nullable EventNode<? extends Event> create(@NotNull Object listener) {
        EventNode<Event> eventNode = EventNode.all(listener.getClass().getName());
        AtomicInteger count = new AtomicInteger(0);
        Utility.getSuperClasses(listener.getClass()).stream()
                .flatMap(type -> Stream.of(type.getDeclaredMethods()))
                .forEach(method -> {
                    Subscribe subscribe = method.getAnnotation(Subscribe.class);
                    if (subscribe == null) {
                        return;
                    }

                    ConditionUtils.is(method.getParameterCount() == 1, "The method " + method + " is subscribing event but parameter count isn't 1");
                    Class<?> parameterType = method.getParameterTypes()[0];

                    val eventType = parameterType.asSubclass(Event.class);
                    val eventListener = EventListener.builder(eventType)
                            .ignoreCancelled(subscribe.ignoreCancelled())
                            .handler(new AnnotatedHandler<>(listener, method))
                            .build();

                    count.incrementAndGet();
                    eventNode.addListener(eventListener);
                });

        if (count.get() > 0)
            return eventNode;
        return null;
    }

    private static class AnnotatedHandler<T> implements Consumer<T> {

        private final Object listener;
        private final MethodHandle method;

        public AnnotatedHandler(Object listener, Method method) {
            this.listener = listener;

            MethodHandle retMethod = null;
            try {
                method.setAccessible(true);
                retMethod = MethodHandles.lookup().unreflect(method);
            } catch (IllegalAccessException e) {
                SneakyThrowUtil.sneakyThrow(e);
            }

            this.method = retMethod;
        }

        @Override
        public void accept(T t) {
            try {
                this.method.invoke(listener, t);
            } catch (Throwable e) {
                SneakyThrowUtil.sneakyThrow(e);
            }
        }
    }

}
