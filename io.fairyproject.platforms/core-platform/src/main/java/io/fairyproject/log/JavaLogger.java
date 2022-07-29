package io.fairyproject.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaLogger implements ILogger {

    private final Logger logger = Logger.getGlobal();

    @Override
    public void info(String message, Object... replace) {
        logger.info(String.format(message, replace));
    }

    @Override
    public void debug(String message, Object... replace) {
        logger.info(String.format(message, replace));
    }

    @Override
    public void warn(String message, Object... replace) {
        logger.warning(String.format(message, replace));
    }

    @Override
    public void error(String message, Object... replace) {
        logger.warning(String.format(message, replace));
    }

    @Override
    public void info(String message, Throwable throwable, Object... replace) {
        logger.log(Level.INFO, String.format(message, replace), throwable);
    }

    @Override
    public void debug(String message, Throwable throwable, Object... replace) {
        logger.log(Level.INFO, String.format(message, replace), throwable);
    }

    @Override
    public void warn(String message, Throwable throwable, Object... replace) {
        logger.log(Level.WARNING, String.format(message, replace), throwable);
    }

    @Override
    public void error(String message, Throwable throwable, Object... replace) {
        logger.log(Level.WARNING, String.format(message, replace), throwable);
    }

    @Override
    public void info(Throwable throwable) {
        logger.log(Level.INFO, "", throwable);
    }

    @Override
    public void debug(Throwable throwable) {
        logger.log(Level.INFO, "", throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        logger.log(Level.WARNING, "", throwable);
    }

    @Override
    public void error(Throwable throwable) {
        logger.log(Level.WARNING, "", throwable);
    }
}
