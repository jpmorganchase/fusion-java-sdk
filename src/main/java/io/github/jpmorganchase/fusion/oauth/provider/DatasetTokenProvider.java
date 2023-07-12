package io.github.jpmorganchase.fusion.oauth.provider;

public interface DatasetTokenProvider {

    default String getDatasetBearerToken(String catalog, String dataset) {
        return null;
    }
}
