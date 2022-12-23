package me.illgilp.worldeditglobalizer.common;

public class IllegalInvocationException extends RuntimeException {
    public IllegalInvocationException() {
    }

    public IllegalInvocationException(String message) {
        super(message);
    }

    public IllegalInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalInvocationException(Throwable cause) {
        super(cause);
    }

    public IllegalInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
