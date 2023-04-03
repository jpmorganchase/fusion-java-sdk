package io.github.jpmorganchase.fusion.oauth.provider;

public interface DatasetTokenProvider {

    String getDatasetBearerToken(String catalog, String dataset);
}
