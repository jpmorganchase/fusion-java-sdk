package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.exception.FusionException;

public class FileDownloadException extends FusionException {

    public FileDownloadException(String s) {
        super(s);
    }

    public FileDownloadException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
