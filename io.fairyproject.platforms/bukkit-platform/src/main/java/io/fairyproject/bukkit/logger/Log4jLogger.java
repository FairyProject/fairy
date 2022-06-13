package io.fairyproject.bukkit.logger;

import io.fairyproject.log.ILogger;
import org.apache.logging.log4j.LogManager;

public class Log4jLogger implements ILogger {
    @Override
    public void info(String message, Object... replace) {
        LogManager.getLogger().info(String.format(message, replace));
    }

    @Override
    public void debug(String message, Object... replace) {
        LogManager.getLogger().debug(String.format(message, replace));
    }

    @Override
    public void warn(String message, Object... replace) {
        LogManager.getLogger().warn(String.format(message, replace));
    }

    @Override
    public void error(String message, Object... replace) {
        LogManager.getLogger().error(String.format(message, replace));
    }

    @Override
    public void info(String message, Throwable throwable, Object... replace) {
        LogManager.getLogger().info(String.format(message, replace), throwable);
    }

    @Override
    public void debug(String message, Throwable throwable, Object... replace) {
        LogManager.getLogger().debug(String.format(message, replace), throwable);
    }

    @Override
    public void warn(String message, Throwable throwable, Object... replace) {
        LogManager.getLogger().warn(String.format(message, replace), throwable);
    }

    @Override
    public void error(String message, Throwable throwable, Object... replace) {
        LogManager.getLogger().error(String.format(message, replace), throwable);
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
