package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.exception.FusionException;

public class ApiInputValidationException extends FusionException {

    public ApiInputValidationException(String s) {
        super(s);
    }

    public ApiInputValidationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
