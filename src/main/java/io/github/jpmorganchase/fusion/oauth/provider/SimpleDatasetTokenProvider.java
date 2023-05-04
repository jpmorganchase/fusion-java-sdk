package io.github.jpmorganchase.fusion.oauth.provider;

import io.github.jpmorganchase.fusion.oauth.exception.OAuthException;
import io.github.jpmorganchase.fusion.oauth.model.BearerToken;
import java.util.Map;

public class SimpleDatasetTokenProvider implements DatasetTokenProvider {

    private static final String BEARER_TOKEN_NOT_FOUND_ERROR = "Bearer token not found for catalog %s and dataset %s";

    private final Map<String, BearerToken> datasetTokens;

    public SimpleDatasetTokenProvider(Map<String, BearerToken> datasetTokens) {
        this.datasetTokens = datasetTokens;
    }

    @Override
    public String getDatasetBearerToken(String catalog, String dataset) {

        String tokenKey = String.format("%s_%s", catalog, dataset);
        if (datasetTokens.containsKey(tokenKey)) {
            return datasetTokens.get(tokenKey).getToken();
        }

        throw new OAuthException(String.format(BEARER_TOKEN_NOT_FOUND_ERROR, catalog, dataset));
    }
}
