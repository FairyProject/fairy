package io.fairyproject.tests.base;

import io.fairyproject.tests.TestingContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class JUnitJupiterBase extends Base {

    @BeforeAll
    public static void init() {
        TestingContext.get().initialize();
    }

    @BeforeEach
    public void beforeEach() {
        this.checkInitRuntime();
    }

    @AfterEach
    public void afterEach() {
        this.cleanup();
    }

}
