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

package io.fairyproject.util.terminable.composite;

import io.fairyproject.util.terminable.Terminable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author lucko
 */
public class AbstractWeakCompositeTerminable implements CompositeTerminable {
    private final Deque<WeakReference<Terminable>> terminableQueue = new ConcurrentLinkedDeque<>();

    protected AbstractWeakCompositeTerminable() {

    }

    @Override
    public CompositeTerminable with(Terminable terminable) {
        Objects.requireNonNull(terminable, "terminable");
        this.terminableQueue.push(new WeakReference<>(terminable));
        return this;
    }

    @Override
    public void close() throws CompositeClosingException {
        List<Exception> caught = new ArrayList<>();
        for (WeakReference<Terminable> ref; (ref = this.terminableQueue.poll()) != null; ) {
            Terminable terminable = ref.get();
            if (terminable == null) {
                continue;
            }

            try {
                terminable.close();
            } catch (Exception e) {
                caught.add(e);
            }
        }

        if (!caught.isEmpty()) {
            throw new CompositeClosingException(caught);
        }
    }

    @Override
    public boolean isClosed() {
        return this.terminableQueue.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .allMatch(Terminable::isClosed);
    }

    @Override
    public void cleanup() {
        this.terminableQueue.removeIf(ref -> {
            Terminable ac = ref.get();
            return ac == null || ac.isClosed();
        });
    }
}
