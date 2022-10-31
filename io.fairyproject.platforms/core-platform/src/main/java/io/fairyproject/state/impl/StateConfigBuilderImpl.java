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

import io.fairyproject.event.EventNode;
import io.fairyproject.state.*;
import io.fairyproject.state.event.StateEvent;
import io.fairyproject.state.event.StateEventFilter;
import io.fairyproject.state.event.StateMachineEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StateConfigBuilderImpl implements StateConfigBuilder {

    private final State state;
    private final EventNode<StateEvent> eventNode;
    private final List<StateHandler> handlers = new ArrayList<>();

    public StateConfigBuilderImpl(@NotNull State state, @NotNull EventNode<StateMachineEvent> eventNode) {
        this.state = state;
        this.eventNode = eventNode.map(state, StateEventFilter.STATE);
    }

    @NotNull
    @Override
    public EventNode<StateEvent> eventNode() {
        return eventNode;
    }

    @Override
    public @NotNull StateConfigBuilder handler(@NotNull StateHandler handler) {
        this.handlers.add(handler);
        return this;
    }

    public StateConfig build(StateMachine stateMachine) {
        return new StateConfigImpl(this.state, stateMachine, this.handlers, this.eventNode);
    }
}
