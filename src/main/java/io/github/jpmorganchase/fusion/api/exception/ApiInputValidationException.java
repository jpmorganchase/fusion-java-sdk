package io.github.jpmorganchase.fusion.api.exception;

import io.github.jpmorganchase.fusion.FusionException;

public class ApiInputValidationException extends FusionException {

    public ApiInputValidationException(String s) {
        super(s);
    }

    public ApiInputValidationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
