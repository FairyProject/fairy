package io.fairyproject.tests.base;

import io.fairyproject.tests.TestingContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class JUnitBase extends Base {

    @BeforeClass
    public static void init() {
        TestingContext.get().initialize();
    }

    @Before
    public void beforeEach() {
        this.checkInitRuntime();
    }

    @After
    public void afterEach() {
        this.cleanup();
    }

}
