package io.github.jpmorganchase.fusion.oauth.exception;

public class OAuthException extends RuntimeException {

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
