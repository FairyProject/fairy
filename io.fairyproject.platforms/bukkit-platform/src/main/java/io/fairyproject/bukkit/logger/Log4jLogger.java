package io.fairyproject.bukkit.logger;

import io.fairyproject.log.ILogger;
import org.apache.logging.log4j.LogManager;

public class Log4jLogger implements ILogger {
    @Override
    public void info(String message, Object... replace) {
        LogManager.getLogger().info(message, replace);
    }

    @Override
    public void debug(String message, Object... replace) {
        LogManager.getLogger().debug(message, replace);
    }

    @Override
    public void warn(String message, Object... replace) {
        LogManager.getLogger().warn(message, replace);
    }

    @Override
    public void error(String message, Object... replace) {
        LogManager.getLogger().error(message, replace);
    }

    @Override
    public void info(String message, Throwable throwable, Object... replace) {
        LogManager.getLogger().info(message, throwable, replace);
    }

    @Override
    public void debug(String message, Throwable throwable, Object... replace) {
        LogManager.getLogger().debug(message, throwable, replace);
    }

    @Override
    public void warn(String message, Throwable throwable, Object... replace) {
        LogManager.getLogger().warn(message, throwable, replace);
    }

    @Override
    public void error(String message, Throwable throwable, Object... replace) {
        LogManager.getLogger().error(message, throwable, replace);
    }

    @Override
    public void info(Throwable throwable) {
        LogManager.getLogger().info(throwable);
    }

    @Override
    public void debug(Throwable throwable) {
        LogManager.getLogger().debug(throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        LogManager.getLogger().warn(throwable);
    }

    @Override
    public void error(Throwable throwable) {
        LogManager.getLogger().error(throwable);
    }
}
