package io.fairytest.state;

import io.fairyproject.event.EventNode;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.state.Signal;
import io.fairyproject.state.State;
import io.fairyproject.state.StateMachine;
import io.fairyproject.state.StateMachineBuilder;
import io.fairyproject.state.event.StateMachineEvent;
import io.fairyproject.state.event.StateMachineStartEvent;
import io.fairyproject.state.event.StateMachineStopEvent;
import io.fairyproject.state.event.StateMachineTransitionEvent;
import io.fairyproject.tests.base.JUnitJupiterBase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class StateMachineTest extends JUnitJupiterBase {

    private enum ExampleState implements State {
        A, B, C
    }

    private enum ExampleSignal implements Signal {
        T1, T2, T3
    }

    @Test
    public void noInitialStateShouldThrowException() {
        StateMachineBuilder builder = StateMachine.builder();
        builder.state(ExampleState.A);
        builder.state(ExampleState.B);
        builder.state(ExampleState.C);

        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void stateMachineTriggerShouldTransform() {
        StateMachineBuilder builder = StateMachine.builder();
        builder.initialState(ExampleState.A);
        builder.state(ExampleState.A);
        builder.state(ExampleState.B);
        builder.state(ExampleState.C);

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
        builder.state(ExampleState.A);
        builder.state(ExampleState.B);
        builder.state(ExampleState.C);

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

    @Test
    public void eventNode() {
        AtomicBoolean eventCalled = new AtomicBoolean(false);
        StateMachineBuilder builder = StateMachine.builder()
                .initialState(ExampleState.A);
        builder.state(ExampleState.A);
        builder.eventNode()
                .addListener(ExampleStateMachineEvent.class, event -> eventCalled.set(true));

        StateMachine stateMachine = builder.build();
        GlobalEventNode.get().call(new ExampleStateMachineEvent(stateMachine));

        Assertions.assertTrue(eventCalled.get());

        stateMachine.stop(Signal.UNDEFINED);
        eventCalled.set(false);
        GlobalEventNode.get().call(new ExampleStateMachineEvent(stateMachine));

        Assertions.assertFalse(eventCalled.get());
    }

    @Test
    public void basicEvents() {
        StateMachineBuilder builder = StateMachine.builder();
        builder.initialState(ExampleState.A);
        builder.state(ExampleState.A);
        builder.state(ExampleState.B);

        AtomicBoolean startCalled = new AtomicBoolean(false);
        AtomicBoolean transitionCalled = new AtomicBoolean(false);
        AtomicBoolean stopCalled = new AtomicBoolean(false);

        EventNode<StateMachineEvent> eventNode = builder.eventNode();
        eventNode.addListener(StateMachineStartEvent.class, event -> startCalled.set(true));
        eventNode.addListener(StateMachineTransitionEvent.class, event -> transitionCalled.set(true));
        eventNode.addListener(StateMachineStopEvent.class, event -> stopCalled.set(true));

        StateMachine stateMachine = builder.build();

        Assertions.assertTrue(startCalled.get());
        Assertions.assertFalse(transitionCalled.get());
        Assertions.assertFalse(stopCalled.get());

        stateMachine.transform(ExampleState.B);

        Assertions.assertTrue(transitionCalled.get());
        Assertions.assertFalse(stopCalled.get());

        stateMachine.stop(Signal.UNDEFINED);

        Assertions.assertTrue(stopCalled.get());
    }

    @RequiredArgsConstructor
    @Getter
    public static class ExampleStateMachineEvent implements StateMachineEvent {

        private final StateMachine stateMachine;

    }

}
