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

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.fairy.util.terminable.Terminable;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Getter
public class EventSubscription<T extends Event> implements Listener, EventExecutor, Terminable {

    private final Class<T> type;
    private final EventPriority priority;

    private final boolean handleSubClasses;

    private final BiConsumer<? super T, Throwable> exceptionHandler;
    private final BiConsumer<EventSubscription<T>, ? super T>[] listeners;

    private final Predicate<T>[] filters;
    private final BiPredicate<EventSubscription<T>, T>[] beforeExpiryTest;
    private final BiPredicate<EventSubscription<T>, T>[] midExpiryTest;
    private final BiPredicate<EventSubscription<T>, T>[] postExpiryTest;

    private final AtomicLong callCount = new AtomicLong(0);
    private final AtomicBoolean active = new AtomicBoolean(true);

    private final Player activePlayer;
    private final String activeMetadata;

    EventSubscription(EventSubscribeBuilder<T> subscribe) {

        this.type = subscribe.getEventType();
        this.priority = subscribe.getPriority();

        this.handleSubClasses = subscribe.isHandleSubClasses();

        this.exceptionHandler = subscribe.getExceptionHandler();

        this.listeners = subscribe.getHandlers().toArray(new BiConsumer[0]);
        this.filters = subscribe.getFilters().toArray(new Predicate[0]);

        this.beforeExpiryTest = subscribe.getBeforeExpiryTest().toArray(new BiPredicate[0]);
        this.midExpiryTest = subscribe.getMidExpiryTest().toArray(new BiPredicate[0]);
        this.postExpiryTest = subscribe.getPostExpiryTest().toArray(new BiPredicate[0]);

        if (subscribe instanceof EventSubscribeBuilder.PlayerEventSubscribeBuilder) {
            final EventSubscribeBuilder<T>.PlayerEventSubscribeBuilder playerBuilder = (EventSubscribeBuilder<T>.PlayerEventSubscribeBuilder) subscribe;
            this.activePlayer = playerBuilder.getPlayer();
            this.activeMetadata = playerBuilder.getMetadata();
        } else {
            this.activePlayer = null;
            this.activeMetadata = null;
        }
    }

    public int getAccessCount() {
        return this.callCount.intValue();
    }

    public void register(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvent(type, this, priority, this, plugin, false);
    }

    public boolean isActive() {
        return this.active.get();
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {

        if (this.handleSubClasses) {
            if (!this.type.isInstance(event)) {
                return;
            }
        } else {
            if (event.getClass() != this.type) {
                return;
            }
        }

        if (!this.active.get()) {
            event.getHandlers().unregister(listener);
            return;
        }

        T eventInstance = this.type.cast(event);

        if (this.testExpiry(ExpiryStage.BEOFORE, listener, eventInstance)) {
            return;
        }

        try {

            for (Predicate<T> filter : this.filters) {
                if (!filter.test(eventInstance)) {
                    return;
                }
            }

            this.testExpiry(ExpiryStage.POST_FILTER, listener, eventInstance);

            for (BiConsumer<EventSubscription<T>, ? super T> realListener : this.listeners) {
                realListener.accept(this, eventInstance);
            }

            this.callCount.incrementAndGet();
        } catch (Throwable throwable) {

            this.exceptionHandler.accept(eventInstance, throwable);

        }

        this.testExpiry(ExpiryStage.POST_EXECUTE, listener, eventInstance);

    }

    private boolean testExpiry(ExpiryStage stage, Listener listener, T event) {
        BiPredicate<EventSubscription<T>, T>[] tests;

        switch (stage) {
            case BEOFORE:
                tests = this.beforeExpiryTest;
                break;
            case POST_FILTER:
                tests = this.midExpiryTest;
                break;
            case POST_EXECUTE:
                tests = this.postExpiryTest;
                break;
            default:
                return false;
        }

        for (BiPredicate<EventSubscription<T>, T> test : tests) {
            if (test.test(this, event)) {
                event.getHandlers().unregister(listener);
                this.active.set(false);
                return true;
            }
        }

        return false;
    }

    @Override
    public void close() throws Exception {
        this.unregister();
    }

    public boolean unregister() {
        if (!this.active.getAndSet(false)) {
            return false;
        }

        unregisterListener(this.type, this);
        if (this.activePlayer != null && this.activeMetadata != null) {
            Events.getSubscriptionList(this.activePlayer).remove(this.activeMetadata);
        }

        return true;
    }

    private static void unregisterListener(Class<? extends Event> eventClass, Listener listener) {
        try {
            // unfortunately we can't cache this reflect call, as the method is static
            Method getHandlerListMethod = eventClass.getMethod("getHandlerList");
            HandlerList handlerList = (HandlerList) getHandlerListMethod.invoke(null);
            handlerList.unregister(listener);
        } catch (Throwable t) {
            // ignored
        }
    }
}
