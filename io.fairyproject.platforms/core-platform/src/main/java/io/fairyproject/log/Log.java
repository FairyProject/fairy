package io.fairyproject.log;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Log {

    private ILogger INSTANCE;

    public ILogger get() {
        if (INSTANCE == null) {
            INSTANCE = new JavaLogger();
        }
        return INSTANCE;
    }

    public void set(ILogger logger) {
        INSTANCE = logger;
    }

    public void info(String message, Object... replace) {
        get().info(message, replace);
    }

    public void debug(String message, Object... replace) {
        get().debug(message, replace);
    }

    public void warn(String message, Object... replace) {
        get().warn(message, replace);
    }

    public void error(String message, Object... replace) {
        get().error(message, replace);
    }

    public void info(String message, Throwable throwable, Object... replace) {
        get().info(message, throwable, replace);
    }

    public void debug(String message, Throwable throwable, Object... replace) {
        get().debug(message, throwable, replace);
    }

    public void warn(String message, Throwable throwable, Object... replace) {
        get().warn(message, throwable, replace);
    }

    public void error(String message, Throwable throwable, Object... replace) {
        get().error(message, throwable, replace);
    }

    public void info(Throwable throwable) {
        get().info(throwable);
    }

    public void debug(Throwable throwable) {
        get().debug(throwable);
    }

    public void warn(Throwable throwable) {
        get().warn(throwable);
    }

    public void error(Throwable throwable) {
        get().error(throwable);
    }

}
