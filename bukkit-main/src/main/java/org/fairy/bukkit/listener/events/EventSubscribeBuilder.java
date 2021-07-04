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

package org.fairy.bukkit.listener.events;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;
import org.fairy.bukkit.util.BukkitUtil;
import org.fairy.bukkit.reflection.resolver.MethodResolver;
import org.fairy.bukkit.reflection.wrapper.MethodWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public EventSubscribeBuilder(Class<T> type) {
        this.eventType = type;
        this.priority = EventPriority.NORMAL;
        this.filters = new ArrayList<>(0);
        this.beforeExpiryTest = new ArrayList<>(0);
        this.midExpiryTest = new ArrayList<>(0);
        this.postExpiryTest = new ArrayList<>(0);
        this.handlers = new ArrayList<>(1);
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

    private Player player;
    private String metadata;

    public EventSubscribeBuilder<T> forPlayer(Player player, String metadata) {
        if (!BukkitUtil.isPlayerEvent(this.eventType)) {
            throw new IllegalStateException("forPlayer() wouldn't work for non player event!");
        }

        this.player = player;
        this.metadata = metadata;
        return this;
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

    public EventSubscription<T> build(Plugin plugin) {

        if (this.player != null) {

            MethodWrapper<Player> method;

            if (PlayerEvent.class.isAssignableFrom(this.eventType)) {
                method = new MethodResolver(PlayerEvent.class).resolveWrapper("getPlayer");
            } else {
                method = new MethodResolver(this.eventType).resolveWrapper("getPlayer");
            }

            if (!method.exists()) {
                throw new IllegalStateException("Couldn't find getPlayer method in event " + this.eventType.getSimpleName() + " !");
            }

            this.filter(event -> method.invoke(event) == player);
        }

        EventSubscription<T> subscription = new EventSubscription<>(this);
        subscription.register(plugin);

        if (this.player != null &&
                this.metadata != null) {

            Events.getSubscriptionList(player).put(metadata, subscription);

        }

        return subscription;

    }
}
