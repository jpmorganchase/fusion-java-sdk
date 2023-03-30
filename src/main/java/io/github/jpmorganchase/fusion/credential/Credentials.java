package io.github.jpmorganchase.fusion.credential;

public interface Credentials {

    CredentialType getCredentialType();

    enum CredentialType {
        SECRET, PASSWORD, BEARER, DATASET
    }
}
