package io.fairyproject.tests;

public enum RuntimeMode {

    /**
     * Global Runtime
     *
     * If runtime was initialized before, it will use the previous runtime and not initializing again
     */
    GLOBAL,

    /**
     * Before all tests runtime
     *
     * This will initialize runtime at the beginning of the test class
     */
    BEFORE_ALL,

    /**
     * Before each test runtime
     *
     * This will initialize runtime at the beginning of each test
     */
    BEFORE_EACH

}
