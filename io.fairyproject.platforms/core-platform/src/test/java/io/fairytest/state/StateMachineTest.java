package io.fairytest.state;

import io.fairyproject.state.Signal;
import io.fairyproject.state.State;
import io.fairyproject.state.StateMachine;
import io.fairyproject.state.StateMachineBuilder;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateMachineTest extends JUnitJupiterBase {

    private enum ExampleState implements State {
        A, B, C
    }

    private enum ExampleSignal implements Signal {
        T1, T2, T3
    }

    @Test
    public void stateMachineTriggerShouldTransform() {
        StateMachineBuilder builder = StateMachine.builder();
        builder.initialState(ExampleState.A);
        builder.state(ExampleState.A)
                .handleStart(() -> System.out.println("A"));
        builder.state(ExampleState.B)
                .handleStart(() -> System.out.println("B"));
        builder.state(ExampleState.C)
                .handleStart(() -> System.out.println("C"));

        builder.transition()
                .when(ExampleSignal.T1)
                .to(ExampleState.B);
        builder.transition()
                .when(ExampleSignal.T2)
                .to(ExampleState.C);
        builder.transition()
                .when(ExampleSignal.T3)
                .to(ExampleState.A);

        // Start at A, current state should be A
        StateMachine stateMachine = builder.build();

        Assertions.assertEquals(ExampleState.A, stateMachine.getCurrentState());

        // Fire T1, current state should be B
        stateMachine.signal(ExampleSignal.T1);

        Assertions.assertEquals(ExampleState.B, stateMachine.getCurrentState());

        // Fire T2, current state should be C
        stateMachine.signal(ExampleSignal.T2);

        Assertions.assertEquals(ExampleState.C, stateMachine.getCurrentState());
    }

    @Test
    public void transitionSpecificState() {
        StateMachineBuilder builder = StateMachine.builder();
        builder.initialState(ExampleState.A);
        builder.state(ExampleState.A)
                .handleStart(() -> System.out.println("A"));
        builder.state(ExampleState.B)
                .handleStart(() -> System.out.println("B"));
        builder.state(ExampleState.C)
                .handleStart(() -> System.out.println("C"));

        // when state is B and T1 is fired, state should be C
        builder.transition()
                .on(ExampleState.B)
                .when(ExampleSignal.T2)
                .to(ExampleState.C);

        StateMachine stateMachine = builder.build();

        stateMachine.signal(ExampleSignal.T2);

        // since T2 is not handled on state A, state should still be A
        Assertions.assertEquals(ExampleState.A, stateMachine.getCurrentState());

        stateMachine.transform(ExampleState.B);
        stateMachine.signal(ExampleSignal.T2);

        // now state is B, T2 should be handled
        Assertions.assertEquals(ExampleState.C, stateMachine.getCurrentState());
    }

}
