package io.github.jpmorganchase.fusion.oauth.exception;

import io.github.jpmorganchase.fusion.FusionException;

public class OAuthException extends FusionException {

    private final String response;

    public OAuthException(String message) {
        super(message);
        this.response = message;
    }

    public OAuthException(String message, String response) {
        super(message);
        this.response = response;
    }

    public OAuthException(String message, String response, Throwable throwable) {
        super(message, throwable);
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
