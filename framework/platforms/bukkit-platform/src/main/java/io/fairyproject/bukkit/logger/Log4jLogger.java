package io.fairyproject.bukkit.logger;

import io.fairyproject.log.ILogger;
import org.apache.logging.log4j.LogManager;

public class Log4jLogger implements ILogger {
    
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger("Fairy");
    
    @Override
    public void info(String message, Object... replace) {
        logger.info(String.format(message, replace));
    }

    @Override
    public void debug(String message, Object... replace) {
        logger.debug(String.format(message, replace));
    }

    @Override
    public void warn(String message, Object... replace) {
        logger.warn(String.format(message, replace));
    }

    @Override
    public void error(String message, Object... replace) {
        logger.error(String.format(message, replace));
    }

    @Override
    public void info(String message, Throwable throwable, Object... replace) {
        logger.info(String.format(message, replace), throwable);
    }

    @Override
    public void debug(String message, Throwable throwable, Object... replace) {
        logger.debug(String.format(message, replace), throwable);
    }

    @Override
    public void warn(String message, Throwable throwable, Object... replace) {
        logger.warn(String.format(message, replace), throwable);
    }

    @Override
    public void error(String message, Throwable throwable, Object... replace) {
        logger.error(String.format(message, replace), throwable);
    }

    @Override
    public void info(Throwable throwable) {
        logger.info(throwable);
    }

    @Override
    public void debug(Throwable throwable) {
        logger.debug(throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        logger.warn(throwable);
    }

    @Override
    public void error(Throwable throwable) {
        logger.error(throwable);
    }
}
