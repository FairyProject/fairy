package io.fairytest.state;

import io.fairyproject.state.StateMachine;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class StateTest extends JUnitJupiterBase {

    @Test
    public void unhandledTrigger() {
        AtomicBoolean unhandledCalled = new AtomicBoolean(false);
        StateMachine<Integer, Integer> stateMachine = StateMachine.create();
        stateMachine.state(1)
                .when(1, t -> t.to(2))
                .unhandled(t -> unhandledCalled.set(true));
        stateMachine.start(1, null);
        stateMachine.fire(2); // call an unhandled trigger

        Assertions.assertTrue(unhandledCalled.get());
    }

}
