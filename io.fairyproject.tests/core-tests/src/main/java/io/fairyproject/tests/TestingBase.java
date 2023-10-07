package io.fairyproject.tests;
;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * @deprecated see {@link io.fairyproject.tests.base.JUnitBase} and {@link io.fairyproject.tests.base.JUnitJupiterBase}
 */
@Deprecated
public abstract class TestingBase {

    @BeforeAll
    public static void init() {
        TestingContext.get().initialize();
    }

    @BeforeEach
    public void beforeEach() {
        // TODO: inject autowired fields
    }

}
