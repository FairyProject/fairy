package io.fairyproject.state;

import io.fairyproject.state.trigger.Trigger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TriggerTaskImpl<S, T> implements TriggerTask<S, T> {

    private final Trigger<T> trigger;
    private final State<S, T> state;
    private final List<Runnable> tasks;

    public TriggerTaskImpl(@NotNull Trigger<T> trigger, @NotNull State<S, T> state) {
        this.trigger = trigger;
        this.state = state;
        this.tasks = new ArrayList<>();
    }

    @Override
    public @NotNull State<S, T> state() {
        return this.state;
    }

    @Override
    public TriggerTask<S, T> run(Runnable runnable) {
        this.tasks.add(runnable);
        return this;
    }

    @Override
    public TriggerTask<S, T> to(S state) {
        return this.run(() -> this.state()
                .machine()
                .swap(state, trigger));
    }

    @Override
    public TriggerTask<S, T> end() {
        return this.run(() -> this.state()
                .machine()
                .stop(null));
    }

    @Override
    public void fire() {
        this.tasks.forEach(Runnable::run);
    }
}
