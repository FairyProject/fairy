package io.fairyproject.event;

import com.google.common.collect.ImmutableMap;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ApiStatus.Experimental
public interface EventBinding<E extends Event> {

    static <E extends Event, T> @NotNull FilteredBuilder<E, T> filtered(@NotNull EventFilter<E, T> filter, @NotNull Predicate<T> predicate) {
        return new FilteredBuilder<>(filter, predicate);
    }

    @NotNull Collection<Class<? extends Event>> eventTypes();

    @NotNull Consumer<@NotNull E> consumer(@NotNull Class<? extends Event> eventType);

    class FilteredBuilder<E extends Event, T> {
        private final EventFilter<E, T> filter;
        private final Predicate<T> predicate;
        private final Map<Class<? extends Event>, BiConsumer<Object, E>> mapped = new HashMap<>();

        FilteredBuilder(EventFilter<E, T> filter, Predicate<T> predicate) {
            this.filter = filter;
            this.predicate = predicate;
        }

        public <M extends E> FilteredBuilder<E, T> map(@NotNull Class<M> eventType,
                                                       @NotNull BiConsumer<@NotNull T, @NotNull M> consumer) {
            //noinspection unchecked
            this.mapped.put(eventType, (BiConsumer<Object, E>) consumer);
            return this;
        }

        public @NotNull EventBinding<E> build() {
            final Map<Class<? extends Event>, BiConsumer<Object, E>> copy = ImmutableMap.copyOf(mapped);
            final Set<Class<? extends Event>> eventTypes = copy.keySet();

            Map<Class<? extends Event>, Consumer<E>> consumers = new HashMap<>(eventTypes.size());
            for (val eventType : eventTypes) {
                val consumer = copy.get(eventType);
                consumers.put(eventType, event -> {
                    final T handler = filter.getHandler(event);
                    if (!predicate.test(handler)) return;
                    consumer.accept(handler, event);
                });
            }
            return new EventBinding<E>() {
                @Override
                public @NotNull Collection<Class<? extends Event>> eventTypes() {
                    return eventTypes;
                }

                @Override
                public @NotNull Consumer<E> consumer(@NotNull Class<? extends Event> eventType) {
                    return consumers.get(eventType);
                }
            };
        }
    }
}
