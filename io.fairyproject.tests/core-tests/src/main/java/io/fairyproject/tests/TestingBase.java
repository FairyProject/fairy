package io.fairyproject.tests;

import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
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
        try {
            AutowiredContainerController.INSTANCE.applyObject(this);
        } catch (ReflectiveOperationException e) {
            SneakyThrowUtil.sneakyThrow(e);
        }
    }

}
