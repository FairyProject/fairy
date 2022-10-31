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
import io.fairyproject.state.State;
import io.fairyproject.state.StateConfig;
import io.fairyproject.state.StateHandler;
import io.fairyproject.state.StateMachine;
import io.fairyproject.state.event.StateEvent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StateConfigImpl implements StateConfig {

    @Getter
    private final State state;
    @NotNull
    private final StateMachine stateMachine;
    private final StateHandler[] handlers;
    private final EventNode<StateEvent> eventNode;

    public StateConfigImpl(@NotNull State state, @NotNull StateMachine stateMachine, @NotNull List<StateHandler> handlers, @NotNull EventNode<StateEvent> eventNode) {
        this.state = state;
        this.stateMachine = stateMachine;
        this.handlers = handlers.toArray(new StateHandler[0]);
        this.eventNode = eventNode;

        stateMachine.getEventNode().addChild(eventNode);
    }

    @Override
    public @NotNull StateMachine getStateMachine() {
        return this.stateMachine;
    }

    @Override
    public @NotNull StateHandler[] getHandlers() {
        return this.handlers;
    }

    @Override
    public @Nullable StateHandler getHandler(@NotNull Class<? extends StateHandler> handlerClass) {
        for (StateHandler handler : this.handlers) {
            if (handler.getClass().equals(handlerClass)) {
                return handler;
            }
        }
        return null;
    }
}
