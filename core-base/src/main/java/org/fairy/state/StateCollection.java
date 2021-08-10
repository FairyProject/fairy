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

package org.fairy.state;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public abstract class StateCollection extends State implements Iterable<State> {

    protected final List<State> states;

    public StateCollection(List<State> states) {
        this.states = states;
    }

    public StateCollection() {
        this(new ArrayList<>());
    }

    public void add(State state) {
        this.states.add(state);
    }

    public void addAll(Collection<State> states) {
        this.states.addAll(states);
    }

    public void pause() {
        super.pause();
        this.states.forEach(State::pause);
    }

    public void unpause() {
        super.unpause();
        this.states.forEach(State::unpause);
    }

    @NotNull
    @Override
    public Iterator<State> iterator() {
        return this.states.iterator();
    }

    public State get(int index) {
        return this.states.get(index);
    }

    public int size() {
        return this.states.size();
    }

    public int lastIndex() {
        return this.size() - 1;
    }

    public boolean allMatch(Predicate<State> statePredicate) {
        for (State state : states) {
            if (!statePredicate.test(state)) {
                return false;
            }
        }
        return true;
    }

    public boolean anyMatch(Predicate<State> statePredicate) {
        for (State state : states) {
            if (statePredicate.test(state)) {
                return true;
            }
        }
        return false;
    }
}
