package com.jpmorganchase.fusion.api;

public class FileDownloadException extends RuntimeException {

    public FileDownloadException(String s) {
        super(s);
    }

    public FileDownloadException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
