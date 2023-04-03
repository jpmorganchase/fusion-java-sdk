package io.github.jpmorganchase.fusion.oauth.provider;

import io.github.jpmorganchase.fusion.oauth.credential.Credentials;

public interface SessionTokenProvider {
    String getSessionBearerToken();

    void updateCredentials(Credentials credentials);
}
