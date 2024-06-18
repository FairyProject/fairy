package io.fairyproject.scheduler.repeat;

import io.fairyproject.scheduler.ScheduledTask;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class LengthRepeatPredicate<T> implements RepeatPredicate<T> {

    private final long startTime;
    private final Duration duration;
    private final T defaultValue;

    @Override
    public boolean shouldContinue(ScheduledTask<?> task) {
        return System.currentTimeMillis() - startTime < duration.toMillis();
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }
}
