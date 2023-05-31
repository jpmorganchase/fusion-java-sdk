package io.github.jpmorganchase.fusion;

public class FusionException extends RuntimeException {
    public FusionException() {
        super();
    }

    public FusionException(String s) {
        super(s);
    }

    public FusionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
