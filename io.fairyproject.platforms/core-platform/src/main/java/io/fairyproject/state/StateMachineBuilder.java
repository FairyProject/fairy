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

package io.fairyproject.state;

import io.fairyproject.event.EventNode;
import io.fairyproject.state.event.StateMachineEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface StateMachineBuilder {

    /**
     * Create a new state from the state machine
     *
     * @param state the state entity
     * @return the state implementation
     */
    @NotNull StateConfigBuilder state(@NotNull State state);

    /**
     * Set the initial state of the state machine
     *
     * @param state the state
     * @return this
     */
    @Contract("_ -> this")
    @NotNull StateMachineBuilder initialState(@NotNull State state);

    /**
     * Transition builder for the state machine
     *
     * @return the transition builder
     */
    @NotNull StateMachineTransitionBuilder transition();

    /**
     * The event node of the state machine
     *
     * @return the event node
     */
    @NotNull EventNode<StateMachineEvent> eventNode();

    /**
     * Build the state machine
     *
     * @return the state machine
     */
    @NotNull StateMachine build();

}
