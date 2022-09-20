package io.fairytest.state.impl;

import io.fairyproject.state.StateMachine;
import io.fairyproject.state.impl.TimeoutStateHandler;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class TimeoutStateHandlerTest extends JUnitJupiterBase {

    private enum ExampleState {
        A, B, C
    }

    private enum ExampleTrigger {
        T1, T2, T3
    }

    // test timeout state handler
    @Test
    public void timeoutStateShouldTrigger() throws InterruptedException {
        StateMachine<ExampleState, ExampleTrigger> stateMachine = StateMachine.create();

        stateMachine.state(ExampleState.A)
                .handler(TimeoutStateHandler.of(Duration.ofSeconds(1)))
                .when(TimeoutStateHandler.trigger(), t -> t.to(ExampleState.B));
        stateMachine.state(ExampleState.B);
        stateMachine.interval(Duration.ofMillis(50L));
        stateMachine.start(ExampleState.A, null);

        long l = System.currentTimeMillis();
        while (stateMachine.current() != ExampleState.B) {
            Thread.sleep(1000L);
            if (System.currentTimeMillis() - l > 2000L) {
                Assertions.fail("Timeout");
            }
        }

        Assertions.assertEquals(ExampleState.B, stateMachine.current());
    }

}
