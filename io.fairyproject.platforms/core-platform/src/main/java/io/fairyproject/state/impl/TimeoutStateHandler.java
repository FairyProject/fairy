package io.fairyproject.state.impl;

import io.fairyproject.state.StateHandler;
import io.fairyproject.state.StateMachine;
import io.fairyproject.state.trigger.Trigger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class TimeoutStateHandler<S, T> implements StateHandler<S, T> {

    public static final Trigger<?> TRIGGER = Trigger.keyed("timeout");

    private final Duration duration;
    private long timestamp;

    public TimeoutStateHandler(@NotNull Duration duration) {
        this.duration = duration;
    }

    @Override
    public void onStart(@NotNull StateMachine<S, T> stateMachine, @NotNull S state, @Nullable Trigger<T> trigger) {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void onTick(@NotNull StateMachine<S, T> stateMachine, @NotNull S state) {
        if (System.currentTimeMillis() - this.timestamp > this.duration.toMillis()) {
            stateMachine.fire(TimeoutStateHandler.trigger());
        }
    }

    @Override
    public void onStop(@NotNull StateMachine<S, T> stateMachine, @NotNull S state, @Nullable Trigger<T> trigger) {
        // do nothing
    }

    public static <T> Trigger<T> trigger() {
        return (Trigger<T>) TRIGGER;
    }

    public static <S, T> TimeoutStateHandler<S, T> of(@NotNull Duration duration) {
        return new TimeoutStateHandler<>(duration);
    }

}
