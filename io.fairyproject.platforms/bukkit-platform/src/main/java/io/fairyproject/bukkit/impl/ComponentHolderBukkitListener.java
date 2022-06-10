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

package io.fairyproject.bukkit.impl;

import io.fairyproject.container.ComponentHolder;
import io.fairyproject.log.Log;
import org.bukkit.event.Listener;
import io.fairyproject.bukkit.listener.FilteredListener;
import io.fairyproject.bukkit.listener.events.Events;

public class ComponentHolderBukkitListener extends ComponentHolder {

    @Override
    public Object newInstance(Class<?> type) {
        Object object = super.newInstance(type);

        if (FilteredListener.class.isAssignableFrom(type)) {
            return object;
        } else if (Listener.class.isAssignableFrom(type)) {
            Events.subscribe((Listener) object);
        } else {
            Log.error("The Class " + type.getSimpleName() + " wasn't implement Listener or FunctionListener!");
            return null;
        }

        return object;
    }

    @Override
    public Class<?>[] type() {
        return new Class[] {FilteredListener.class, Listener.class};
    }
}
