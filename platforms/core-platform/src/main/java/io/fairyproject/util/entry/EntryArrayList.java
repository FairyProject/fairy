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

package io.fairyproject.util.entry;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class EntryArrayList<K, V> extends ArrayList<Entry<K, V>> {

    public void add(K k, V v) {
        this.add(new Entry<>(k, v));
    }

    public boolean remove(K k, V v) {
        return this.remove(new Entry<>(k, v));
    }

    public boolean removeIf(BiPredicate<K, V> filter) {
        return this.removeIf(entry -> filter.test(entry.getKey(), entry.getValue()));
    }

    public void forEach(BiConsumer<K, V> consumer) {
        this.forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
    }
}
