package io.fairytest.state.impl;

import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.Schedulers;
import io.fairyproject.state.State;
import io.fairyproject.state.StateMachine;
import io.fairyproject.state.StateMachineBuilder;
import io.fairyproject.state.impl.TimeoutStateHandler;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class TimeoutStateHandlerTest extends JUnitJupiterBase {

    private enum ExampleState implements State {
        A, B, C
    }

    // test timeout state handler
    @Test
    public void timeoutStateShouldTrigger() throws InterruptedException {
        StateMachineBuilder builder = StateMachine.builder();
        builder.initialState(ExampleState.A);

        builder.state(ExampleState.A)
                .handler(TimeoutStateHandler.of(Duration.ofMillis(500)));

        builder.transition()
                .when(TimeoutStateHandler.SIGNAL)
                .to(ExampleState.B);

        builder.state(ExampleState.B);
        StateMachine stateMachine = builder.build();

        ScheduledTask<?> task = Schedulers.IO.scheduleAtFixedRate(stateMachine::tick, Duration.ofMillis(100), Duration.ofMillis(100));

        long l = System.currentTimeMillis();
        while (stateMachine.getCurrentState() != ExampleState.B) {
            Thread.sleep(100L);
            if (System.currentTimeMillis() - l > 2000L) {
                Assertions.fail("Timeout");
            }
        }

        task.cancel();

        Assertions.assertEquals(ExampleState.B, stateMachine.getCurrentState());
    }

}
