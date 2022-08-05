package io.fairyproject.log;

public interface ILogger {

    void info(String message, Object... replace);

    void debug(String message, Object... replace);

    void warn(String message, Object... replace);

    void error(String message, Object... replace);

    void info(String message, Throwable throwable, Object... replace);

    void debug(String message, Throwable throwable, Object... replace);

    void warn(String message, Throwable throwable, Object... replace);

    void error(String message, Throwable throwable, Object... replace);

    void info(Throwable throwable);

    void debug(Throwable throwable);

    void warn(Throwable throwable);

    void error(Throwable throwable);

}
