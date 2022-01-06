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

package io.fairyproject.bukkit.listener;

import com.google.common.collect.ImmutableList;
import io.fairyproject.util.terminable.TerminableConsumer;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ListenerSubscription implements AutoCloseable {

    private final AtomicBoolean registered;
    private final List<Listener> listeners;
    private final Plugin plugin;
    private final TerminableConsumer terminableConsumer;

    public ListenerSubscription(Listener[] listeners, Plugin plugin, @Nullable TerminableConsumer terminableConsumer) {
        this.listeners = ImmutableList.copyOf(listeners);
        this.registered = new AtomicBoolean(false);
        this.plugin = plugin;
        this.terminableConsumer = terminableConsumer;
    }

    public void register() {
        if (!this.registered.compareAndSet(false, true)) {
            return;
        }
        this.listeners.forEach(listener -> {
            try {
                Bukkit.getPluginManager().registerEvents(listener, this.plugin);
            } catch (Throwable throwable) {
                LogManager.getLogger().error("Error while registering listener " + listener.getClass().getName(), throwable);
            }
        });
        if (this.terminableConsumer != null) {
            this.terminableConsumer.bind(this);
        }
    }

    @Override
    public void close() {
        if (!this.registered.compareAndSet(true, false)) {
            return;
        }
        this.listeners.forEach(HandlerList::unregisterAll);
    }
}
