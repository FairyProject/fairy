package io.fairytest.state;

import io.fairyproject.state.StateMachine;
import io.fairyproject.state.TriggerTask;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateMachineTest extends JUnitJupiterBase {

    private enum ExampleState {
        A, B, C
    }

    private enum ExampleTrigger {
        T1, T2, T3
    }

    @Test
    public void stateMachineTriggerShouldTransform() {
        StateMachine<ExampleState, ExampleTrigger> stateMachine = StateMachine.create();

        stateMachine.state(ExampleState.A)
                .handleStart(t -> System.out.println("A"))
                .when(ExampleTrigger.T1, trigger -> trigger.to(ExampleState.B));
        stateMachine.state(ExampleState.B)
                .handleStart(t -> System.out.println("B"))
                .when(ExampleTrigger.T2, trigger -> trigger.to(ExampleState.C));
        stateMachine.state(ExampleState.C)
                .handleStart(t -> System.out.println("C"))
                .when(ExampleTrigger.T3, TriggerTask::end);

        // Start at A, current state should be A
        stateMachine.start(ExampleState.A, null);

        Assertions.assertEquals(ExampleState.A, stateMachine.current());

        // Fire T1, current state should be B
        stateMachine.fire(ExampleTrigger.T1);

        Assertions.assertEquals(ExampleState.B, stateMachine.current());

        // Fire T2, current state should be C
        stateMachine.fire(ExampleTrigger.T2);

        Assertions.assertEquals(ExampleState.C, stateMachine.current());
    }

}
