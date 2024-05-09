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

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.event.*;
import org.bukkit.plugin.*;

@NoArgsConstructor
public class FilteredListener<T extends Plugin> implements Listener {

    @Getter
    private FilteredEventList eventList;
    public T plugin;

    public FilteredListener(FilteredEventList eventList, T plugin) {
        this.initial(plugin, eventList);
    }

    public void initial(T plugin, FilteredEventList eventChecker) {
        this.plugin = plugin;
        this.eventList = eventChecker;
        FilteredListenerRegistry.INSTANCE.register(this);
    }

}
