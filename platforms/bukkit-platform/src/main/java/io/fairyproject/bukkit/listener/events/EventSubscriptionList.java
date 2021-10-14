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

import java.util.LinkedHashMap;

public class EventSubscriptionList extends LinkedHashMap<String, EventSubscription<?>> {

    @Override
    public EventSubscription<?> get(Object key) {
        return super.getOrDefault(key, null);
    }

    @Override
    public EventSubscription<?> remove(Object key) {
        return this.remove(key, true);
    }

    public EventSubscription<?> remove(Object key, boolean unregister) {
        EventSubscription<?> subscription = super.remove(key);
        if (unregister && subscription != null && subscription.isActive()) {
            subscription.unregister();
        }
        return subscription;
    }

    @Override
    public void clear() {

        synchronized (this) {
            for (EventSubscription<?> eventSubscription : this.values()) {
                if (eventSubscription.isActive()) {
                    eventSubscription.unregister();
                }
            }
        }

        super.clear();

    }
}
