package io.github.jpmorganchase.fusion.exception;

public class FusionException extends RuntimeException {

    public FusionException() {
        super();
    }

    public FusionException(String message) {
        super(message);
    }

    public FusionException(String message, Throwable cause) {
        super(message, cause);
    }
}
