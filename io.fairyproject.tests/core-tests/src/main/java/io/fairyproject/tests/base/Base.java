package io.fairyproject.tests.base;

import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.tests.RuntimeMode;
import io.fairyproject.tests.TestingContext;
import io.fairyproject.tests.TestingHandle;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import lombok.Getter;

abstract class Base {

    @Getter
    private boolean initialized;

    public final void checkInitRuntime() {
        switch (this.runtimeMode()) {
            case GLOBAL:
                break;
            case BEFORE_ALL:
                if (this.initialized) {
                    break;
                }
                this.initialized = true;
            case BEFORE_EACH:
                this.initRuntime();
                break;
        }

        this.initAutowired();
    }

    public void cleanup() {
        TestingContext.get().setDirty();
    }

    public void initRuntime() {
        this.initRuntime(null);
    }

    public void initRuntime(TestingHandle testingHandle) {
        if (TestingContext.get().isClean()) {
            return;
        }
        if (TestingContext.get().isInitialized()) {
            TestingContext.get().shutdown();
        }
        if (testingHandle == null)
            TestingContext.get().initialize();
        else
            TestingContext.get().initialize(testingHandle);
    }

    public void initAutowired() {
        try {
            AutowiredContainerController.INSTANCE.applyObject(this);
        } catch (ReflectiveOperationException e) {
            SneakyThrowUtil.sneakyThrow(e);
        }
    }

    public RuntimeMode runtimeMode() {
        return RuntimeMode.GLOBAL;
    }

}
