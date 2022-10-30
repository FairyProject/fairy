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

import io.fairyproject.Fairy;
import io.fairyproject.event.EventFilter;
import io.fairyproject.event.EventNode;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.state.*;
import io.fairyproject.state.event.StateMachineEvent;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateMachineImpl implements StateMachine {

    private final Map<State, StateConfig> states;
    private final EventNode<StateMachineEvent> eventNode;

    private CompositeTerminable compositeTerminable;
    @Setter
    private Transition transition;
    @Setter
    private Duration interval;

    private State current;
    private boolean running;

    public StateMachineImpl() {
        this.states = new ConcurrentHashMap<>();
        this.eventNode = EventNode.type("state-machine", EventFilter.from(StateMachineEvent.class, null, null));
        GlobalEventNode.get().addChild(this.eventNode);
    }

    @Override
    public @Nullable State getCurrentState() {
        return this.current;
    }

    @Override
    public EventNode<StateMachineEvent> getEventNode() {
        return eventNode;
    }

    public void addState(@NotNull State state, @NotNull StateConfig stateConfig) {
        this.states.put(state, stateConfig);
    }

    public void start(State state) {
        this.current = state;
        StateConfig config = this.states.get(this.current);
        for (StateHandler handler : config.getHandlers()) {
            handler.onStart(this, this.current, Signal.UNDEFINED);
        }

        this.compositeTerminable = CompositeTerminable.create();
        this.running = true;

        // schedule a task to tick the state machine
        if (this.interval != null) {
            int ticks = (int) (this.interval.toMillis() / 50);
            this.bind(Fairy.getTaskScheduler().runRepeated(this::tick, 50, ticks));
        }
    }

    @Override
    public @Nullable State transform(@NotNull State state, @NotNull Signal signal) {
        State previous = this.current;

        if (previous != null) {
            StateConfig previousConfig = this.states.get(previous);
            for (StateHandler handler : previousConfig.getHandlers()) {
                handler.onStop(this, previous, signal);
            }
        }

        this.current = state;
        StateConfig current = this.states.get(state);
        for (StateHandler handler : current.getHandlers()) {
            handler.onStart(this, state, signal);
        }

        return previous;
    }

    @Override
    public void tick() {
        StateConfig config = this.states.get(this.current);
        if (config == null)
            throw new IllegalArgumentException("State " + this.current + " does not exist");

        for (StateHandler handler : config.getHandlers()) {
            handler.onTick(this, this.current);
        }
    }

    @Override
    public void stop(Signal signal) {
        if (this.current != null) {
            StateConfig currentState = this.states.get(this.current);
            for (StateHandler handler : currentState.getHandlers()) {
                handler.onStop(this, this.current, signal);
            }
        }

        this.running = false;
        this.current = null;
        this.compositeTerminable.closeAndReportException();
        GlobalEventNode.get().removeChild(this.eventNode);
    }

    @Override
    public @NotNull Duration getInterval() {
        return this.interval;
    }

    @Override
    public void signal(@NotNull Signal signal) {
        this.transition.handle(signal);
    }

    @Override
    public void close() throws Exception {
        this.stop(Signal.UNDEFINED);
    }

    @Override
    public boolean isClosed() {
        return !this.running;
    }

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }
}
