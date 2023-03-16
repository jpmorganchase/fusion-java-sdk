package io.github.jpmorganchase.fusion.credential;

import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.http.JdkClient;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OAuthCredentials implements Credentials {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String clientId;
    private final String resource;
    private final String authServerUrl;
    private String bearerToken;
    private long bearerTokenExpiry;
    private int tokenRefreshes;

    private final Client httpClient;

    private final TimeProvider timeProvider;

    public OAuthCredentials(String clientId, String resource, String authServerUrl) {
        this(clientId, resource, authServerUrl, new JdkClient());
    }

    public OAuthCredentials(String clientId, String resource, String authServerUrl, Client client) {
        this(clientId, resource, authServerUrl, client, new SystemTimeProvider());
    }

    public OAuthCredentials(
            String clientId, String resource, String authServerUrl, Client client, TimeProvider timeProvider) {
        this.clientId = clientId;
        this.resource = resource;
        this.authServerUrl = authServerUrl;
        tokenRefreshes = 0;
        bearerTokenExpiry = 0L;
        httpClient = client;
        this.timeProvider = timeProvider;
    }

    @Override
    public final synchronized String getBearerToken() {

        if (hasTokenExpired()) {

            Map<String, String> requestHeaders = new HashMap<>();
            if (requiresAuthHeader()) {
                requestHeaders.put("Authorization", getAuthHeader());
            }
            requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
            requestHeaders.put("Accept", "application/json");

            HttpResponse<String> response = httpClient.post(authServerUrl, requestHeaders, getPostBodyContent());
            if (response.isError()) {
                throw new OAuthException(
                        String.format(
                                "Error response received from OAuth server with status code %s",
                                response.getStatusCode()),
                        response.getBody());
            }

            // Get the bearer token
            OAuthServerResponse oAuthServerResponse = OAuthServerResponse.fromJson(response.getBody());
            this.bearerToken = oAuthServerResponse.getAccessToken();
            if (bearerToken == null) {
                String message = "Unable to parse bearer token in response from OAuth server";
                logger.error(String.format(message)); // Don't log the response body in case it has a token
                throw new OAuthException(message, response.getBody());
            }

            // Get the token expiry time
            Instant now = Instant.ofEpochMilli(timeProvider.currentTimeMillis());
            Instant expiryTime = now.plusSeconds(oAuthServerResponse.getExpiresIn() - 30);
            this.bearerTokenExpiry = expiryTime.toEpochMilli();

            tokenRefreshes++;
            logger.atInfo()
                    .setMessage("Token expires at: {}")
                    // TODO: This wont necessarily be accurate for a non-standard TimeProvider implementation
                    .addArgument(expiryTime.atZone(ZoneId.systemDefault()))
                    .log();
            logger.atInfo()
                    .setMessage("Number of token refreshes: {}")
                    .addArgument(this.tokenRefreshes)
                    .log();
        }
        return bearerToken;
    }

    private boolean hasTokenExpired() {
        return bearerTokenExpiry < timeProvider.currentTimeMillis();
    }

    protected abstract String getPostBodyContent();

    protected abstract boolean requiresAuthHeader();

    protected abstract String getAuthHeader();

    public String getClientId() {
        return clientId;
    }

    public String getResource() {
        return resource;
    }

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public long getBearerTokenExpiry() {
        return bearerTokenExpiry;
    }

    public int getTokenRefreshes() {
        return tokenRefreshes;
    }
}
