package io.github.jpmorganchase.fusion.credential;

public class OAuthException extends RuntimeException {

    private final String response;

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
