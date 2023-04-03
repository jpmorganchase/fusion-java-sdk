package io.github.jpmorganchase.fusion.oauth.provider;

import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.oauth.credential.OAuthDatasetCredentials;
import io.github.jpmorganchase.fusion.oauth.model.BearerToken;
import io.github.jpmorganchase.fusion.oauth.retriever.OAuthTokenRetriever;
import io.github.jpmorganchase.fusion.time.SystemTimeProvider;
import io.github.jpmorganchase.fusion.time.TimeProvider;
import java.util.HashMap;
import java.util.Map;

public class OAuthDatasetTokenProvider implements DatasetTokenProvider {

    private static final String FUSION_AUTH_URL_POSTFIX = "catalogs/%s/datasets/%s/authorize/token";

    private final String fusionAuthUrl;
    private final SessionTokenProvider sessionTokenProvider;
    private final OAuthTokenRetriever tokenRetriever;
    private final TimeProvider timeProvider;

    private final Map<String, BearerToken> datasetTokens;

    public OAuthDatasetTokenProvider(
            String fusionAuthUrl, SessionTokenProvider sessionTokenProvider, Client httpClient) {
        this(fusionAuthUrl, sessionTokenProvider, new OAuthTokenRetriever(httpClient));
    }

    public OAuthDatasetTokenProvider(
            String fusionAuthUrl, SessionTokenProvider sessionTokenProvider, OAuthTokenRetriever tokenRetriever) {
        this(fusionAuthUrl, sessionTokenProvider, tokenRetriever, new SystemTimeProvider(), new HashMap<>());
    }

    public OAuthDatasetTokenProvider(String fusionAuthUrl, SessionTokenProvider sessionTokenProvider) {
        this(fusionAuthUrl, sessionTokenProvider, new OAuthTokenRetriever(), new SystemTimeProvider(), new HashMap<>());
    }

    public OAuthDatasetTokenProvider(
            String fusionAuthUrl,
            SessionTokenProvider sessionTokenProvider,
            OAuthTokenRetriever tokenRetriever,
            TimeProvider timeProvider,
            Map<String, BearerToken> datasetTokens) {
        this.fusionAuthUrl = fusionAuthUrl + FUSION_AUTH_URL_POSTFIX;
        this.tokenRetriever = tokenRetriever;
        this.sessionTokenProvider = sessionTokenProvider;
        this.timeProvider = timeProvider;
        this.datasetTokens = new HashMap<>(datasetTokens);
    }

    @Override
    public synchronized String getDatasetBearerToken(String catalog, String dataset) {

        String datasetTokenKey = String.format("%s_%s", catalog, dataset);
        if (datasetTokens.containsKey(datasetTokenKey)) {
            BearerToken bearerToken = datasetTokens.get(datasetTokenKey);

            if (!bearerToken.hasTokenExpired(timeProvider.currentTimeMillis())) {
                return bearerToken.getToken();
            }
        }

        BearerToken bearerToken = tokenRetriever.retrieve(OAuthDatasetCredentials.builder()
                .token(sessionTokenProvider.getSessionBearerToken())
                .catalog(catalog)
                .dataset(dataset)
                .authServerUrl(this.fusionAuthUrl)
                .build());

        datasetTokens.put(datasetTokenKey, bearerToken);
        return bearerToken.getToken();
    }
}
