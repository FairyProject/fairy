/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.bukkit.listener.events;

import com.google.common.base.Preconditions;
import io.fairyproject.bukkit.player.PlayerEventRecognizer;
import io.fairyproject.util.terminable.TerminableConsumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @deprecated Use {@link io.fairyproject.bukkit.events.BukkitEventNode} instead.
 */
@Deprecated
@Getter
public class EventSubscribeBuilder<T extends Event> {

    private final Class<T> eventType;
    private EventPriority priority;

    private boolean handleSubClasses;

    private BiConsumer<? super T, Throwable> exceptionHandler = (t, throwable) -> {
        throw new RuntimeException(throwable);
    };

    private final List<Predicate<T>> filters;
    private final List<BiPredicate<EventSubscription<T>, T>> beforeExpiryTest;
    private final List<BiPredicate<EventSubscription<T>, T>> midExpiryTest;
    private final List<BiPredicate<EventSubscription<T>, T>> postExpiryTest;

    private final List<BiConsumer<EventSubscription<T>, ? super T>> handlers;
    private Plugin plugin;
    private TerminableConsumer bindWith;

    public EventSubscribeBuilder(Class<T> type) {
        this.eventType = type;
        this.priority = EventPriority.NORMAL;
        this.filters = new ArrayList<>(0);
        this.beforeExpiryTest = new ArrayList<>(0);
        this.midExpiryTest = new ArrayList<>(0);
        this.postExpiryTest = new ArrayList<>(0);
        this.handlers = new ArrayList<>(1);
    }

    public EventSubscribeBuilder(EventSubscribeBuilder<T> original) {
        this.eventType = original.getEventType();
        this.priority = original.getPriority();
        this.filters = original.getFilters();
        this.beforeExpiryTest = original.getBeforeExpiryTest();
        this.midExpiryTest = original.getMidExpiryTest();
        this.postExpiryTest = original.getPostExpiryTest();
        this.handlers = original.getHandlers();
        this.handleSubClasses = original.isHandleSubClasses();
        this.exceptionHandler = original.getExceptionHandler();
        this.plugin = original.getPlugin();
        this.bindWith = original.getBindWith();
    }

    public EventSubscribeBuilder<T> priority(EventPriority priority) {
        this.priority = priority;
        return this;
    }

    public EventSubscribeBuilder<T> filter(Predicate<T> filter) {
        this.filters.add(filter);
        return this;
    }

    public EventSubscribeBuilder<T> handleException(BiConsumer<? super T, Throwable> consumer) {
        this.exceptionHandler = consumer;
        return this;
    }

    public EventSubscribeBuilder<T> handleSubClasses() {
        this.handleSubClasses = true;
        return this;
    }

    public EventSubscribeBuilder<T> expireAfter(long duration, @NonNull TimeUnit unit) {
        Preconditions.checkArgument(duration >= 1, "duration < 1");
        long expiry = Math.addExact(System.currentTimeMillis(), unit.toMillis(duration));
        return expireIf((handler, event) -> System.currentTimeMillis() > expiry, ExpiryStage.BEOFORE);
    }

    public EventSubscribeBuilder<T> expireAfterAccess(long maxCalls) {
        Preconditions.checkArgument(maxCalls >= 1, "maxCalls < 1");
        return expireIf((handler, event) -> handler.getAccessCount() >= maxCalls, ExpiryStage.BEOFORE, ExpiryStage.POST_EXECUTE);
    }

    public PlayerEventSubscribeBuilder forPlayer(Player player) {
        return this.forPlayer(player, null);
    }

    public PlayerEventSubscribeBuilder forPlayer(Player player, String metadata) {
        return new PlayerEventSubscribeBuilder(this, player, metadata);
    }

    public EventSubscribeBuilder<T> expireIf(@NonNull BiPredicate<EventSubscription<T>, T> predicate, @NonNull ExpiryStage... stages) {
        for (ExpiryStage stage : stages) {
            switch (stage) {
                case BEOFORE:
                    this.beforeExpiryTest.add(predicate);
                    break;
                case POST_FILTER:
                    this.midExpiryTest.add(predicate);
                    break;
                case POST_EXECUTE:
                    this.postExpiryTest.add(predicate);
                    break;
            }
        }
        return this;
    }

    @SafeVarargs
    public final EventSubscribeBuilder<T> listen(BiConsumer<EventSubscription<T>, T>... listeners) {
        this.handlers.addAll(Arrays.asList(listeners));
        return this;
    }

    @SafeVarargs
    public final EventSubscribeBuilder<T> listen(Consumer<T>... listeners) {
        this.handlers.addAll(Stream.of(listeners)
                .map(consumer -> (BiConsumer<EventSubscription<T>, T>) (tEventSubscription, t) -> consumer.accept(t))
                .collect(Collectors.toList())
        );
        return this;
    }

    public final EventSubscribeBuilder<T> plugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public final EventSubscribeBuilder<T> bindWith(TerminableConsumer terminableConsumer) {
        this.bindWith = terminableConsumer;
        return this;
    }

    public EventSubscription<T> build(Plugin plugin) {
        this.plugin = plugin;
        return this.build();
    }

    public EventSubscription<T> build() {
        if (this.plugin == null) {
            throw new IllegalArgumentException("No plugin were registered in EventSubscribeBuilder.");
        }

        EventSubscription<T> subscription = new EventSubscription<>(this);
        subscription.register(this.plugin);
        if (this.bindWith != null) {
            subscription.bindWith(this.bindWith);
        }

        return subscription;
    }

    @RequiredArgsConstructor
    private static class PlayerPredicate<T extends Event> implements Predicate<T> {

        private final UUID uuid;
        private final Class<PlayerEventRecognizer.Attribute<?>>[] attributes;

        @Override
        public boolean test(T event) {
            final Player player = PlayerEventRecognizer.tryRecognize(event, attributes);
            if (player != null) {
                return player.getUniqueId().equals(this.uuid);
            }
            return false;
        }
    }

    @Getter
    public class PlayerEventSubscribeBuilder extends EventSubscribeBuilder<T> {

        private final Player player;
        private final String metadata;
        private final List<Class<PlayerEventRecognizer.Attribute<?>>> recognizeAttributes;

        public PlayerEventSubscribeBuilder(EventSubscribeBuilder<T> original, Player player, String metadata) {
            super(original);
            this.player = player;
            this.metadata = metadata;
            this.recognizeAttributes = new ArrayList<>(0);
        }

        public PlayerEventSubscribeBuilder recognizeAttribute(Class<PlayerEventRecognizer.Attribute<?>>... attributes) {
            this.recognizeAttributes.addAll(Arrays.asList(attributes));
            return this;
        }

        @Override
        public EventSubscription<T> build(Plugin plugin) {
            if (this.recognizeAttributes.isEmpty() && !PlayerEventRecognizer.isTypePossible(this.getEventType())) {
                throw new IllegalStateException("used forPlayer() but type " + this.getEventType().getSimpleName() + " seems to be impossible to get Player!");
            }
            this.filter(new PlayerPredicate<>(this.player.getUniqueId(), this.recognizeAttributes.toArray(new Class[0])));

            EventSubscription<T> subscription = new EventSubscription<>(this);
            subscription.register(plugin);

            if (this.metadata != null) {
                Events.getSubscriptionList(player).put(metadata, subscription);
            }

            return subscription;
        }

    }
}
