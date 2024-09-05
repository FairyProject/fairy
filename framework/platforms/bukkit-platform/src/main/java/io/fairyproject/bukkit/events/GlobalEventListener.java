/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.bukkit.events;

import io.fairyproject.bukkit.events.handler.HandlerListCollection;
import io.fairyproject.bukkit.listener.RegisterAsListener;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.log.Log;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

@InjectableComponent
@RequiredArgsConstructor
@RegisterAsListener
public class GlobalEventListener implements Listener {

    private final List<Consumer<Event>> listeners = new ArrayList<>();

    @PostInitialize
    public void onPostInitialize() {
        Log.info("Attempting to inject global event listeners...");

        try {
            Field allLists = HandlerList.class.getDeclaredField("allLists");
            allLists.setAccessible(true);

            allLists.set(null, new HandlerListCollection(HandlerList.getHandlerLists(), this));
        } catch (Throwable throwable) {
            Log.error("Failed to inject global event listeners, some features may not work properly.", throwable);
            throwable.printStackTrace();
            return;
        }

        Log.info("Successfully injected global event listeners.");
    }

    public void addListener(Consumer<Event> listener) {
        this.listeners.add(listener);
    }

    public void onEventFired(Event event) {
        for (Consumer<Event> listener : this.listeners) {
            listener.accept(event);
        }
    }

}
