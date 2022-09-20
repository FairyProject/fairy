package io.fairyproject.state;

import io.fairyproject.Fairy;
import io.fairyproject.state.trigger.Trigger;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class StateMachineImpl<S, T> implements StateMachine<S, T> {

    private final Map<S, State<S, T>> states;

    private CompositeTerminable compositeTerminable;

    private S current;

    private Duration interval;
    private boolean running;

    public StateMachineImpl() {
        this.states = new HashMap<>();
    }

    @Override
    public @NotNull State<S, T> state(@NotNull S s) {
        return this.states.computeIfAbsent(s, ignored -> new StateImpl<>(this));
    }

    @Override
    public @NotNull StateMachine<S, T> interval(Duration interval) {
        this.interval = interval;
        return this;
    }

    @Override
    public @Nullable S current() {
        return this.current;
    }

    @Override
    public @NotNull StateMachine<S, T> start(@NotNull S state, @Nullable Trigger<T> trigger) {
        if (trigger == null)
            trigger = Trigger.start();

        this.current = state;
        State<S, T> currentState = this.states.get(this.current);
        @Nullable Trigger<T> t = trigger;
        currentState.handlers().forEach(handler -> handler.onStart(this, this.current, t));

        this.compositeTerminable = CompositeTerminable.create();
        this.running = true;

        // schedule a task to tick the state machine
        if (this.interval != null) {
            int ticks = (int) (this.interval.toMillis() / 50);
            this.bind(Fairy.getTaskScheduler().runRepeated(this::tick, 50, ticks));
        }

        return this;
    }

    @Override
    public @Nullable S swap(@NotNull S state, @NotNull Trigger<T> trigger) {
        S previous = this.current;

        if (previous != null) {
            State<S, T> previousState = this.states.get(previous);
            previousState.handlers().forEach(handler -> handler.onStop(this, previous, trigger));
        }

        this.current = state;
        State<S, T> current = this.states.get(state);
        current.handlers().forEach(handler -> handler.onStart(this, state, trigger));

        return previous;
    }

    @Override
    public @NotNull StateMachine<S, T> tick() {
        State<S, T> state = this.states.get(this.current);
        if (state == null)
            throw new IllegalArgumentException("State " + this.current + " does not exist");

        state.handlers().forEach(stateHandler -> stateHandler.onTick(this, this.current));
        return this;
    }

    @Override
    public @NotNull StateMachine<S, T> stop(@Nullable Trigger<T> trigger) {
        if (trigger == null)
            trigger = Trigger.end();

        if (this.current != null) {
            State<S, T> currentState = this.states.get(this.current);
            @Nullable Trigger<T> t = trigger;
            currentState.handlers().forEach(handler -> handler.onStop(this, this.current, t));
        }

        this.running = false;
        this.current = null;
        this.compositeTerminable.closeAndReportException();

        return this;
    }

    @Override
    public @NotNull Duration interval() {
        return this.interval;
    }

    @Override
    public @NotNull StateMachine<S, T> fire(@NotNull Trigger<T> trigger) {
        State<S, T> state = this.states.get(this.current);
        if (state == null)
            throw new IllegalArgumentException("State " + this.current + " does not exist");

        state.fire(trigger);
        return this;
    }

    @Override
    public void close() throws Exception {
        this.stop(null);
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
