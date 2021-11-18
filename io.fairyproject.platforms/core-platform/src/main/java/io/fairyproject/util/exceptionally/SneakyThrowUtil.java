package io.fairyproject.util.exceptionally;

final class SneakyThrowUtil {

    private SneakyThrowUtil() {
    }

    static <T extends Exception, R> R sneakyThrow(Exception t) throws T {
        throw (T) t;
    }
}
