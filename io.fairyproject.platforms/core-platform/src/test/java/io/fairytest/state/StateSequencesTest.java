package io.fairytest.state;

import io.fairyproject.state.StateBase;
import io.fairyproject.state.StateSequences;
import io.fairyproject.tests.TestingBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateSequencesTest extends TestingBase {

    @Test
    public void sequenceEndingFirstNaturally() {
        ExState a = new ExState() {
            @Override
            protected boolean canEnd() {
                return true;
            }
        };

        ExState b = new ExState();
        StateSequences states = new StateSequences(a, b);
        states.start();

        states.update();
        Assertions.assertTrue(a.start);
        Assertions.assertTrue(a.end);
        Assertions.assertTrue(b.start);
    }

    @Test
    public void sequenceSkipTo() {
        ExState a = new ExState();
        ExState b = new ExState();
        ExState c = new ExState();
        ExState d = new ExState();

        StateSequences states = new StateSequences(a, b, c, d);
        states.start();

        states.skip(3);
        states.update();
        Assertions.assertTrue(a.start);
        Assertions.assertTrue(a.end);

        Assertions.assertTrue(b.start);
        Assertions.assertTrue(b.end);

        Assertions.assertTrue(c.start);
        Assertions.assertTrue(c.end);

        Assertions.assertTrue(d.start);
        Assertions.assertFalse(d.end);
    }

    @Test
    public void sequenceSkipToWithoutNotifyOthers() {
        ExState a = new ExState();
        ExState b = new ExState();
        ExState c = new ExState();
        ExState d = new ExState();

        StateSequences states = new StateSequences(a, b, c, d);
        states.start();

        states.skip(3, true);
        states.update();
        Assertions.assertTrue(a.start);
        Assertions.assertTrue(a.end);

        Assertions.assertFalse(b.start);
        Assertions.assertFalse(b.end);

        Assertions.assertFalse(c.start);
        Assertions.assertFalse(c.end);

        Assertions.assertTrue(d.start);
        Assertions.assertFalse(d.end);
    }

    private static class ExState extends StateBase {

        private boolean start;
        private boolean end;

        @Override
        protected void onStart() {
            this.start = true;
        }

        @Override
        protected void onUpdate() {
            // nothing
        }

        @Override
        protected void onEnded() {
            this.end = true;
        }
    }

}
