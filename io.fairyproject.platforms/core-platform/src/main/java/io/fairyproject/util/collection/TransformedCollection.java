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

package io.fairyproject.util.collection;

import io.fairyproject.util.ConditionUtils;
import lombok.RequiredArgsConstructor;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class TransformedCollection<F, T> extends AbstractCollection<T> {
    final Collection<F> fromCollection;
    final Function<F, T> function;

    @Override
    public void clear() {
        fromCollection.clear();
    }

    @Override
    public boolean isEmpty() {
        return fromCollection.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return new TransformedIterator<F, T>(fromCollection.iterator(), function);
    }

    @Override
    public Spliterator<T> spliterator() {
        return map(fromCollection.spliterator(), function);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        ConditionUtils.notNull(action, "action");
        fromCollection.forEach((F f) -> action.accept(function.apply(f)));
    }

    @Override
    public boolean removeIf(java.util.function.Predicate<? super T> filter) {
        ConditionUtils.notNull(filter, "filter");
        return fromCollection.removeIf(element -> filter.test(function.apply(element)));
    }

    @Override
    public int size() {
        return fromCollection.size();
    }

    static <F, T> Spliterator<T> map(
            Spliterator<F> fromSpliterator, Function<? super F, ? extends T> function) {
        ConditionUtils.notNull(fromSpliterator, "fromSpliterator");
        ConditionUtils.notNull(function, "function");
        return new Spliterator<T>() {

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                return fromSpliterator.tryAdvance(
                        fromElement -> action.accept(function.apply(fromElement)));
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                fromSpliterator.forEachRemaining(fromElement -> action.accept(function.apply(fromElement)));
            }

            @Override
            public Spliterator<T> trySplit() {
                Spliterator<F> fromSplit = fromSpliterator.trySplit();
                return (fromSplit != null) ? map(fromSplit, function) : null;
            }

            @Override
            public long estimateSize() {
                return fromSpliterator.estimateSize();
            }

            @Override
            public int characteristics() {
                return fromSpliterator.characteristics()
                        & ~(Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);
            }
        };
    }
}