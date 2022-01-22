package io.fairytest.state;

import io.fairyproject.state.StateBase;
import io.fairyproject.state.StateSequences;
import io.fairyproject.tests.TestingBase;
import org.junit.Assert;
import org.junit.Test;

public class StateSequencesTest extends TestingBase {

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
        Assert.assertTrue(a.start);
        Assert.assertTrue(a.end);

        Assert.assertTrue(b.start);
        Assert.assertTrue(b.end);

        Assert.assertTrue(c.start);
        Assert.assertTrue(c.end);

        Assert.assertTrue(d.start);
        Assert.assertFalse(d.end);
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
        Assert.assertTrue(a.start);
        Assert.assertTrue(a.end);

        Assert.assertFalse(b.start);
        Assert.assertFalse(b.end);

        Assert.assertFalse(c.start);
        Assert.assertFalse(c.end);

        Assert.assertTrue(d.start);
        Assert.assertFalse(d.end);
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
