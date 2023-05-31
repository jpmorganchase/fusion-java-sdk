package io.github.jpmorganchase.fusion.api.exception;

import io.github.jpmorganchase.fusion.FusionException;

public class FileDownloadException extends FusionException {

    public FileDownloadException(String s) {
        super(s);
    }

    public FileDownloadException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
