package io.github.jpmorganchase.fusion.oauth.provider;

import io.github.jpmorganchase.fusion.oauth.credential.Credentials;

public interface SessionTokenProvider {
    default String getSessionBearerToken(){return null;}

    default void updateCredentials(Credentials credentials) {}
}
