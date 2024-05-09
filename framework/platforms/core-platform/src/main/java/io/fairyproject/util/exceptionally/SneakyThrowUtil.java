package io.fairyproject.util.exceptionally;

public final class SneakyThrowUtil {

    private SneakyThrowUtil() {
    }

    public static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
