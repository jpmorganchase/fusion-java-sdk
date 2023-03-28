package io.github.jpmorganchase.fusion.credential;

public interface OAuthSessionTokenProvider {
    String getSessionBearerToken();
    void updateCredentials(Credentials credentials);

}
