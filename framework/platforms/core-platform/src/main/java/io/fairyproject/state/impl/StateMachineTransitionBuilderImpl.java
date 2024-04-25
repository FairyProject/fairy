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

package io.fairyproject.state.impl;

import io.fairyproject.state.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class StateMachineTransitionBuilderImpl extends TransitionBuilderImpl implements StateMachineTransitionBuilder {

    private final Map<State, TransitionBuilderImpl> stateTransitions = new HashMap<>();

    @Override
    public @NotNull TransitionBuilder on(@NotNull State state) {
        return this.stateTransitions.computeIfAbsent(state, s -> new TransitionBuilderImpl());
    }

    @Override
    public @NotNull StateMachineTransitionBuilder when(@NotNull Signal signal, @NotNull TransitionHandle handle) {
        return (StateMachineTransitionBuilder) super.when(signal, handle);
    }

    @Override
    public @NotNull StateMachineTransitionBuilder when(@NotNull Signal signal, @NotNull Consumer<TransitionHandle> handle) {
        return (StateMachineTransitionBuilder) super.when(signal, handle);
    }

    @Override
    public @NotNull StateMachineTransitionBuilder when(@NotNull Signal signal, @NotNull Runnable runnable) {
        return (StateMachineTransitionBuilder) super.when(signal, runnable);
    }

    @Override
    public Transition build(StateMachine stateMachine) {
        Transition transition = super.build(stateMachine);

        return signal -> {
            State state = stateMachine.getCurrentState();
            TransitionBuilderImpl builder = this.stateTransitions.get(state);
            if (builder != null) {
                Transition stateTransition = builder.build(stateMachine);
                stateTransition.handle(signal);
            }

            transition.handle(signal);
        };
    }
}
