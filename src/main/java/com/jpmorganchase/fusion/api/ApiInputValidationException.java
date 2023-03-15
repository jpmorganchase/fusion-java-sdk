package com.jpmorganchase.fusion.api;

public class ApiInputValidationException extends RuntimeException {

    public ApiInputValidationException(String s) {
        super(s);
    }

    public ApiInputValidationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
