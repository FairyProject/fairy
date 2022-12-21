package io.fairytest.state.impl;

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
        builder.interval(Duration.ofMillis(50));

        StateMachine stateMachine = builder.build();
        long l = System.currentTimeMillis();
        while (stateMachine.getCurrentState() != ExampleState.B) {
            Thread.sleep(100L);
            if (System.currentTimeMillis() - l > 2000L) {
                Assertions.fail("Timeout");
            }
        }

        Assertions.assertEquals(ExampleState.B, stateMachine.getCurrentState());
    }

}
