package io.github.jpmorganchase.fusion.oauth.credential;

import lombok.Value;

@Value
public class BearerTokenCredentials implements Credentials {

    String bearerToken;

    @Override
    public CredentialType getCredentialType() {
        return CredentialType.BEARER;
    }
}
