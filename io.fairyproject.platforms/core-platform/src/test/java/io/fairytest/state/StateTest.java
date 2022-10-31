/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairytest.state;

import io.fairyproject.event.EventNode;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.state.Signal;
import io.fairyproject.state.State;
import io.fairyproject.state.StateMachine;
import io.fairyproject.state.StateMachineBuilder;
import io.fairyproject.state.event.StateEvent;
import io.fairyproject.state.event.StateEventFilter;
import io.fairyproject.state.event.StateStartEvent;
import io.fairyproject.state.event.StateStopEvent;
import io.fairyproject.tests.base.JUnitJupiterBase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class StateTest extends JUnitJupiterBase {

    @Test
    public void stateWithSameNameShouldBeEqual() {
        State a = State.of("state");
        State b = State.of("state");

        Assertions.assertEquals(a, b);
    }

    @Test
    public void stateWithDifferentNameShouldNotBeEqual() {
        State a = State.of("state");
        State b = State.of("state2");

        Assertions.assertNotEquals(a, b);
    }

    @Test
    public void eventNode() {
        State a = State.of("a");
        State b = State.of("b");

        AtomicBoolean eventOnA = new AtomicBoolean(false);
        AtomicBoolean eventOnB = new AtomicBoolean(false);

        StateMachineBuilder builder = StateMachine.builder();
        builder.initialState(a);
        builder.state(a)
                .eventNode()
                .addListener(ExampleStateEvent.class, event -> {
                    eventOnA.set(true);
                });
        builder.state(b)
                .eventNode()
                .addListener(ExampleStateEvent.class, event -> {
                    eventOnB.set(true);
                });

        StateMachine stateMachine = builder.build();
        GlobalEventNode.get().call(new ExampleStateEvent(stateMachine, a));

        Assertions.assertTrue(eventOnA.get());
        Assertions.assertFalse(eventOnB.get());

        eventOnA.set(false);
        eventOnB.set(false);

        GlobalEventNode.get().call(new ExampleStateEvent(stateMachine, b));

        Assertions.assertFalse(eventOnA.get());
        Assertions.assertTrue(eventOnB.get());

        // when the state machine is shutdown, the event node should be shutdown too
        stateMachine.stop(Signal.UNDEFINED);

        eventOnA.set(false);
        eventOnB.set(false);

        GlobalEventNode.get().call(new ExampleStateEvent(stateMachine, a));

        Assertions.assertFalse(eventOnA.get());
        Assertions.assertFalse(eventOnB.get());
    }

    @Test
    public void basicEvents() {
        State a = State.of("a");
        State b = State.of("b");

        boolean[] startCall = {false, false};
        boolean[] stopCall = {false, false};

        StateMachineBuilder builder = StateMachine.builder();
        builder.initialState(a);
        builder.state(a);
        builder.state(b);

        EventNode<StateEvent> eventNode = EventNode.type("state-test", StateEventFilter.STATE);
        eventNode.addListener(StateStartEvent.class, event -> {
            if (event.getState() == a) {
                startCall[0] = true;
            } else if (event.getState() == b) {
                startCall[1] = true;
            }
        });
        eventNode.addListener(StateStopEvent.class, event -> {
            if (event.getState() == a) {
                stopCall[0] = true;
            } else if (event.getState() == b) {
                stopCall[1] = true;
            }
        });
        GlobalEventNode.get().addChild(eventNode);
        try {
            StateMachine stateMachine = builder.build();

            Assertions.assertArrayEquals(new boolean[]{true, false}, startCall);
            Assertions.assertArrayEquals(new boolean[]{false, false}, stopCall);

            Arrays.fill(startCall, false);
            Arrays.fill(stopCall, false);

            stateMachine.transform(b);

            Assertions.assertArrayEquals(new boolean[]{false, true}, startCall);
            Assertions.assertArrayEquals(new boolean[]{true, false}, stopCall);

            Arrays.fill(startCall, false);
            Arrays.fill(stopCall, false);

            stateMachine.stop(Signal.UNDEFINED);

            Assertions.assertArrayEquals(new boolean[]{false, false}, startCall);
            Assertions.assertArrayEquals(new boolean[]{false, true}, stopCall);
        } finally {
            GlobalEventNode.get().removeChild(eventNode);
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class ExampleStateEvent implements StateEvent {

        private final StateMachine stateMachine;
        private final State state;

    }
}
