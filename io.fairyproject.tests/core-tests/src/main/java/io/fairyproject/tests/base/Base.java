package io.fairyproject.tests.base;

import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.tests.RuntimeMode;
import io.fairyproject.tests.TestingContext;
import io.fairyproject.tests.TestingHandle;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

abstract class Base {

    private static final Set<Class<?>> INITIALIZED_CLASSES = ConcurrentHashMap.newKeySet();

    public final void checkInitRuntime() {
        switch (this.runtimeMode()) {
            case BEFORE_ALL:
                final Class<? extends Base> baseClass = this.getClass();
                if (INITIALIZED_CLASSES.contains(baseClass)) {
                    break;
                }
                INITIALIZED_CLASSES.add(baseClass);
            case BEFORE_EACH:
                this.initRuntime();
                break;
            default:
            case GLOBAL:
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
