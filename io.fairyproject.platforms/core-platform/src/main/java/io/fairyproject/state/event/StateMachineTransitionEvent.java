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

package io.fairyproject.state.event;

import io.fairyproject.event.Cancellable;
import io.fairyproject.state.Signal;
import io.fairyproject.state.State;
import io.fairyproject.state.StateMachine;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StateMachineTransitionEvent implements StateEvent, Cancellable {

    private final StateMachine stateMachine;
    @Getter
    @Nullable
    private final State from;
    @Getter
    @NotNull
    private final State to;

    @Getter
    @NotNull
    private final Signal signal;

    public StateMachineTransitionEvent(StateMachine stateMachine, @Nullable State from, @NotNull State to, @NotNull Signal signal) {
        this.stateMachine = stateMachine;
        this.from = from;
        this.to = to;
        this.signal = signal;
    }

    @Override
    public @NotNull State getState() {
        return this.to;
    }

    @Override
    public @NotNull StateMachine getStateMachine() {
        return this.stateMachine;
    }
}
