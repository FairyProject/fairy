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
import io.fairyproject.event.EventNode;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.state.*;
import io.fairyproject.state.event.*;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateMachineImpl implements StateMachine {

    private final Map<State, StateConfig> states;
    private EventNode<StateMachineEvent> eventNode;

    private CompositeTerminable compositeTerminable;
    @Setter
    private Transition transition;
    @Setter
    private Duration interval;

    private State current;
    private boolean running;

    public StateMachineImpl() {
        this.states = new ConcurrentHashMap<>();
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

    public void start(State state, EventNode<StateMachineEvent> eventNode) {
        this.current = state;
        this.eventNode = eventNode;

        GlobalEventNode.get().addChild(this.eventNode);

        StateConfig config = this.states.get(this.current);
        if (config == null)
            throw new IllegalStateException("State " + this.current + " is not registered");
        this.onStateStart(config, Signal.UNDEFINED);

        this.compositeTerminable = CompositeTerminable.create();
        this.running = true;

        // schedule a task to tick the state machine
        if (this.interval != null) {
            int ticks = (int) (this.interval.toMillis() / 50);
            this.bind(Fairy.getTaskScheduler().runRepeated(this::tick, 50, ticks));
        }

        GlobalEventNode.get().call(new StateMachineStartEvent(this, state));
    }

    @Override
    public @Nullable State transform(@NotNull State state, @NotNull Signal signal) {
        State previous = this.current;

        StateMachineTransitionEvent event = new StateMachineTransitionEvent(this, previous, state, signal);
        GlobalEventNode.get().callCancellable(event, () -> {
            if (previous != null) {
                StateConfig previousConfig = this.states.get(previous);
                this.onStateStop(previousConfig, signal);
            }

            this.current = state;
            StateConfig current = this.states.get(state);
            this.onStateStart(current, signal);
        });
        return previous;
    }

    @Override
    public void tick() {
        StateConfig config = this.states.get(this.current);
        if (config == null)
            throw new IllegalArgumentException("State " + this.current + " does not exist");

        this.onStateTick(config);
    }

    @Override
    public void stop(Signal signal) {
        State state = this.current;

        if (this.current != null) {
            StateConfig config = this.states.get(this.current);
            this.onStateStop(config, signal);
        }

        this.running = false;
        this.current = null;
        this.compositeTerminable.closeAndReportException();

        GlobalEventNode.get().call(new StateMachineStopEvent(this, state, signal));
        GlobalEventNode.get().removeChild(this.eventNode);
    }

    private void onStateStart(StateConfig stateConfig, Signal signal) {
        for (StateHandler handler : stateConfig.getHandlers()) {
            handler.onStart(this, this.current, signal);
        }

        GlobalEventNode.get().call(new StateStartEvent(this, this.current, signal));
    }

    private void onStateTick(StateConfig stateConfig) {
        for (StateHandler handler : stateConfig.getHandlers()) {
            handler.onTick(this, this.current);
        }
    }

    private void onStateStop(StateConfig stateConfig, Signal signal) {
        for (StateHandler handler : stateConfig.getHandlers()) {
            handler.onStop(this, this.current, signal);
        }

        GlobalEventNode.get().call(new StateStopEvent(this, this.current, signal));
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
