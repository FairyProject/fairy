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

package io.fairyproject.state;

import com.google.common.collect.Lists;

import java.util.List;

public class StateSequences extends StateCollection {

    private int index = 0;
    private boolean skipping = false;

    public StateSequences(List<State> states) {
        super(states);
    }

    public StateSequences(State... states) {
        super(Lists.newArrayList(states));
    }

    public void skip() {
        this.skipping = true;
    }

    public void addNext(State state) {
        this.states.add(this.index, state);
    }

    public void addNext(List<State> states) {
        int i = 0;
        for (State state : states) {
            this.states.add(this.index + i, state);
            i++;
        }
    }

    @Override
    protected void onStart() {
        if (this.states.isEmpty()) {
            this.end();
            return;
        }

        this.getCurrentState().start();
    }

    @Override
    protected void onUpdate() {
        final State currentState = this.getCurrentState();
        currentState.update();

        final boolean readyToEnd = currentState.isReadyToEnd();
        if (readyToEnd && !currentState.isPaused() || skipping) {
            if (this.skipping) {
                if (!readyToEnd) {
                    currentState.onSuspend();
                }
                this.skipping = false;
            }

            currentState.end();
            this.index++;

            if (this.index > this.lastIndex()) {
                this.end();
                return;
            }

            this.getCurrentState().start();
        }
    }

    @Override
    protected boolean canEnd() {
        return this.index == this.lastIndex() && this.getCurrentState().isReadyToEnd();
    }

    @Override
    protected void onEnded() {
        if (this.index < this.size()) {
            this.getCurrentState().end();
        }
    }

    public State getCurrentState() {
        return this.get(this.index);
    }
}