package io.github.jpmorganchase.fusion.credential;

import java.util.HashMap;
import java.util.Map;

public class OAuthProviderAwareDatasetTokenProvider implements OAuthDatasetTokenProvider {

    private static final String FUSION_AUTH_URL_POSTFIX = "catalogs/%s/datasets/%s/authorize/token";

    private final String fusionAuthUrl;
    private final OAuthSessionTokenProvider sessionTokenProvider;
    private final OAuthTokenRetriever tokenRetriever;
    private final TimeProvider timeProvider;
    private final Map<String, BearerToken> datasetTokens = new HashMap<>();

    public OAuthProviderAwareDatasetTokenProvider(String fusionAuthUrl, OAuthSessionTokenProvider sessionTokenProvider){
        this(fusionAuthUrl, sessionTokenProvider, new OAuthTokenRetriever(), new SystemTimeProvider());
    }

    public OAuthProviderAwareDatasetTokenProvider(String fusionAuthUrl, OAuthSessionTokenProvider sessionTokenProvider, OAuthTokenRetriever tokenRetriever, TimeProvider timeProvider) {
        this.fusionAuthUrl = fusionAuthUrl + FUSION_AUTH_URL_POSTFIX;
        this.tokenRetriever = tokenRetriever;
        this.sessionTokenProvider = sessionTokenProvider;
        this.timeProvider = timeProvider;
    }

    @Override
    public synchronized String getDatasetBearerToken(String catalog, String dataset) {

        String datasetTokenKey = String.format("%s_%s", catalog, dataset);
        if (datasetTokens.containsKey(datasetTokenKey)){
            BearerToken bearerToken = datasetTokens.get(datasetTokenKey);

            if (!bearerToken.hasTokenExpired(timeProvider.currentTimeMillis())){
                return bearerToken.getToken();
            }

        }

        BearerToken bearerToken = tokenRetriever.retrieveWithDatasetCredentials(
                OAuthDatasetCredentials.builder()
                        .token(sessionTokenProvider.getSessionBearerToken())
                        .catalog(catalog)
                        .dataset(dataset)
                        .authServerUrl(this.fusionAuthUrl)
                        .build());

        datasetTokens.put(datasetTokenKey, bearerToken);
        return bearerToken.getToken();
    }


}
