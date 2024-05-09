package io.fairyproject.tests.logger;

import io.fairyproject.log.ILogger;

public class DebugLogger implements ILogger {
    @Override
    public void info(String message, Object... replace) {
        System.out.printf((message) + "%n", replace);
    }

    @Override
    public void debug(String message, Object... replace) {
        System.out.printf((message) + "%n", replace);
    }

    @Override
    public void warn(String message, Object... replace) {
        System.err.printf((message) + "%n", replace);
    }

    @Override
    public void error(String message, Object... replace) {
        System.err.printf((message) + "%n", replace);
    }

    @Override
    public void info(String message, Throwable throwable, Object... replace) {
        System.out.printf((message) + "%n", replace);
        throwable.printStackTrace(System.out);
    }

    @Override
    public void debug(String message, Throwable throwable, Object... replace) {
        System.out.printf((message) + "%n", replace);
        throwable.printStackTrace(System.out);
    }

    @Override
    public void warn(String message, Throwable throwable, Object... replace) {
        System.err.printf((message) + "%n", replace);
        throwable.printStackTrace(System.err);
    }

    @Override
    public void error(String message, Throwable throwable, Object... replace) {
        System.err.printf((message) + "%n", replace);
        throwable.printStackTrace(System.err);
    }

    @Override
    public void info(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void debug(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void warn(Throwable throwable) {
        throwable.printStackTrace(System.err);
    }

    @Override
    public void error(Throwable throwable) {
        throwable.printStackTrace(System.err);
    }
}
