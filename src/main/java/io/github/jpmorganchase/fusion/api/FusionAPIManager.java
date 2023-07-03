package io.github.jpmorganchase.fusion.api;

import static io.github.jpmorganchase.fusion.api.tools.ResponseChecker.checkResponseStatus;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.oauth.provider.SessionTokenProvider;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;

/**
 * Class that manages calls to the API. Intended to be called from multithreaded code.
 */
@Builder
public class FusionAPIManager implements APIManager {

    private final Client httpClient;
    private final SessionTokenProvider sessionTokenProvider;

    public void updateBearerToken(String token) {
        sessionTokenProvider.updateCredentials(new BearerTokenCredentials(token));
    }

    /**
     * Call the API with the path provided and return the JSON response.
     *
     * @param apiPath appended to the base URL
     */
    @Override
    public String callAPI(String apiPath) throws APICallException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + sessionTokenProvider.getSessionBearerToken());

        HttpResponse<String> response = httpClient.get(apiPath, requestHeaders);
        checkResponseStatus(response);
        return response.getBody();
    }
}
