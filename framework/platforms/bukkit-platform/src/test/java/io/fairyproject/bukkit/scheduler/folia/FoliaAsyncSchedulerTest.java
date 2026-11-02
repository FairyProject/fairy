package io.fairyproject.bukkit.scheduler.folia;

import io.fairyproject.scheduler.ScheduledTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

class FoliaAsyncSchedulerTest {

    private FoliaAsyncScheduler scheduler;
    private ScheduledTask<?> task;

    @BeforeEach
    void setUp() {
        // real methods everywhere except the overload that talks to the Folia AsyncScheduler
        scheduler = Mockito.mock(FoliaAsyncScheduler.class, Mockito.CALLS_REAL_METHODS);
        task = Mockito.mock(ScheduledTask.class);
        doReturn(task).when(scheduler).schedule(any(Callable.class), any(Duration.class));
    }

    @Test
    void scheduleRunnableWithDurationDelegatesToCallable() throws Exception {
        AtomicBoolean ran = new AtomicBoolean();

        assertSame(task, scheduler.schedule(() -> ran.set(true), Duration.ofMillis(250)));
        assertDelegated(Duration.ofMillis(250), ran);
    }

    @Test
    void scheduleRunnableWithTicksDelegatesToCallable() throws Exception {
        AtomicBoolean ran = new AtomicBoolean();

        assertSame(task, scheduler.schedule(() -> ran.set(true), 5L));
        assertDelegated(Duration.ofMillis(250), ran);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void assertDelegated(Duration delay, AtomicBoolean ran) throws Exception {
        ArgumentCaptor<Callable> callable = ArgumentCaptor.forClass(Callable.class);
        verify(scheduler).schedule(callable.capture(), eq(delay));

        callable.getValue().call();
        assertTrue(ran.get());
    }

}
