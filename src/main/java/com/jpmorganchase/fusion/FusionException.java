package com.jpmorganchase.fusion;

public class FusionException extends RuntimeException {

    public FusionException(String s) {
        super(s);
    }

    public FusionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
