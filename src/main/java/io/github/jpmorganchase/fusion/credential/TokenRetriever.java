package io.github.jpmorganchase.fusion.credential;

public interface TokenRetriever {
    BearerToken retrieveWithPasswordCredentials(OAuthPasswordBasedCredentials credentials);

    BearerToken retrieveWithSecretCredentials(OAuthSecretBasedCredentials credentials);

    BearerToken retrieveForDatasetWithFusionCredentials(FusionCredentials fusionCredentials);
}
