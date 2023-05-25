package io.github.jpmorganchase.fusion.api;

public class FileUploadException extends RuntimeException {

    public FileUploadException(String s) {
        super(s);
    }

    public FileUploadException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
