package io.github.jpmorganchase.fusion;

public class FusionInitialisationException extends RuntimeException {

    public FusionInitialisationException(String s) {
        super(s);
    }

    public FusionInitialisationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
