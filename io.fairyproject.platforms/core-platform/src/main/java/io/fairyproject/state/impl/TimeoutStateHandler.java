package io.fairyproject.state.impl;

import io.fairyproject.state.Signal;
import io.fairyproject.state.State;
import io.fairyproject.state.StateHandler;
import io.fairyproject.state.StateMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class TimeoutStateHandler implements StateHandler {

    public static final Signal SIGNAL = Signal.of("timeout");

    private final Duration duration;
    private long timestamp;

    public TimeoutStateHandler(@NotNull Duration duration) {
        this.duration = duration;
    }

    @Override
    public void onStart(@NotNull StateMachine stateMachine, @NotNull State state, @Nullable Signal signal) {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void onTick(@NotNull StateMachine stateMachine, @NotNull State state) {
        if (System.currentTimeMillis() - this.timestamp > this.duration.toMillis()) {
            stateMachine.signal(SIGNAL);
        }
    }

    public static TimeoutStateHandler of(@NotNull Duration duration) {
        return new TimeoutStateHandler(duration);
    }

}
