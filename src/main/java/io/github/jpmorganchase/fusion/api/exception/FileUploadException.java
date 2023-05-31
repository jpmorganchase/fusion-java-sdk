package io.github.jpmorganchase.fusion.api.exception;

import io.github.jpmorganchase.fusion.FusionException;

public class FileUploadException extends FusionException {

    public FileUploadException(String s) {
        super(s);
    }

    public FileUploadException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
