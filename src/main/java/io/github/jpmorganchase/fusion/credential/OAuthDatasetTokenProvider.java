package io.github.jpmorganchase.fusion.credential;

public interface OAuthDatasetTokenProvider {

    String getDatasetBearerToken(String catalog, String dataset);
}
