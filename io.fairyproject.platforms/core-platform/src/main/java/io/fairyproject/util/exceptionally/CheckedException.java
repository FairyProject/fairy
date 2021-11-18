package io.fairyproject.util.exceptionally;

public class CheckedException extends RuntimeException {
    public CheckedException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
