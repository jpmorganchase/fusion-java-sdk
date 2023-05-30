package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.exception.FusionException;

public class FileUploadException extends FusionException {

    public FileUploadException(String s) {
        super(s);
    }

    public FileUploadException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
