package io.github.jpmorganchase.fusion.http;

public class ClientException extends RuntimeException {

    public ClientException(String s) {
        super(s);
    }

    public ClientException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
