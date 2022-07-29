package io.fairyproject.app.logger;

import io.fairyproject.log.ILogger;
import org.tinylog.Logger;

public class TinyLogger implements ILogger {
    @Override
    public void info(String message, Object... replace) {
        Logger.info(message, replace);
    }

    @Override
    public void debug(String message, Object... replace) {
        Logger.debug(message, replace);
    }

    @Override
    public void warn(String message, Object... replace) {
        Logger.warn(message, replace);
    }

    @Override
    public void error(String message, Object... replace) {
        Logger.error(message, replace);
    }

    @Override
    public void info(String message, Throwable throwable, Object... replace) {
        Logger.info(message, throwable, replace);
    }

    @Override
    public void debug(String message, Throwable throwable, Object... replace) {
        Logger.debug(message, throwable, replace);
    }

    @Override
    public void warn(String message, Throwable throwable, Object... replace) {
        Logger.warn(message, throwable, replace);
    }

    @Override
    public void error(String message, Throwable throwable, Object... replace) {
        Logger.error(message, throwable, replace);
    }

    @Override
    public void info(Throwable throwable) {
        Logger.info(throwable);
    }

    @Override
    public void debug(Throwable throwable) {
        Logger.debug(throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        Logger.warn(throwable);
    }

    @Override
    public void error(Throwable throwable) {
        Logger.error(throwable);
    }
}
