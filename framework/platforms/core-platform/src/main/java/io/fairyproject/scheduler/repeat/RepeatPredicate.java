package io.fairyproject.scheduler.repeat;

import io.fairyproject.scheduler.ScheduledTask;

import java.time.Duration;

public interface RepeatPredicate<T> {

    static <T> RepeatPredicate<T> cycled(int cycles) {
        return new CycledRepeatPredicate<>(cycles, null);
    }

    static <T> RepeatPredicate<T> cycled(int cycles, T defaultValue) {
        return new CycledRepeatPredicate<>(cycles, defaultValue);
    }

    static <T> RepeatPredicate<T> length(long startTime, Duration duration, T defaultValue) {
        return new LengthRepeatPredicate<>(startTime, duration, defaultValue);
    }

    static <T> RepeatPredicate<T> length(Duration duration, T defaultValue) {
        return length(System.currentTimeMillis(), duration, defaultValue);
    }

    static <T> RepeatPredicate<T> length(Duration duration) {
        return length(duration, null);
    }

    @SuppressWarnings("unchecked")
    static <T> RepeatPredicate<T> empty() {
        return (RepeatPredicate<T>) new EmptyRepeatPredicate();
    }

    boolean shouldContinue(ScheduledTask<?> task);

    T getDefaultValue();

}
