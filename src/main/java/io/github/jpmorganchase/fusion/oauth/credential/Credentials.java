package io.github.jpmorganchase.fusion.oauth.credential;

public interface Credentials {

    CredentialType getCredentialType();

    enum CredentialType {
        SECRET,
        PASSWORD,
        BEARER,
        DATASET
    }
}
