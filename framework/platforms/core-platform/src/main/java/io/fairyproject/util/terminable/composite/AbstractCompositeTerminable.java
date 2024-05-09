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

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AbstractCompositeTerminable implements CompositeTerminable {
    private final Deque<Terminable> terminableQueue = new ConcurrentLinkedDeque<>();

    protected AbstractCompositeTerminable() {

    }

    @Override
    public CompositeTerminable with(Terminable terminable) {
        Objects.requireNonNull(terminable, "terminable");
        this.terminableQueue.push(terminable);
        return this;
    }

    @Override
    public void close() throws CompositeClosingException {
        List<Exception> caught = new ArrayList<>();
        for (Terminable ac; (ac = this.terminableQueue.poll()) != null; ) {
            try {
                ac.close();
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
        return this.terminableQueue.stream().allMatch(closable -> closable instanceof Terminable && ((Terminable) closable).isClosed());
    }

    @Override
    public void cleanup() {
        this.terminableQueue.removeIf(ac -> {
            if (!(ac instanceof Terminable)) {
                return false;
            }
            if (ac instanceof CompositeTerminable) {
                ((CompositeTerminable) ac).cleanup();
            }
            return ((Terminable) ac).isClosed();
        });
    }
}
