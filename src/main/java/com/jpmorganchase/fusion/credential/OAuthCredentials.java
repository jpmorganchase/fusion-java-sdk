package com.jpmorganchase.fusion.credential;

import com.jpmorganchase.fusion.http.Client;
import com.jpmorganchase.fusion.http.HttpResponse;
import com.jpmorganchase.fusion.http.JdkClient;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.Calendar;
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
    public final synchronized String getBearerToken() throws IOException {

        if (hasTokenExpired()) {

            Map<String, String> requestHeaders = new HashMap<>();
            if (requiresAuthHeader()) {
                requestHeaders.put("Authorization", getAuthHeader());
            }
            requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
            requestHeaders.put("Accept", "application/json");

            HttpResponse<String> response = httpClient.post(authServerUrl, requestHeaders, getPostBodyContent());

            // TODO: Error Handling?
            // Get the bearer token
            OAuthServerResponse oAuthServerResponse = OAuthServerResponse.fromJson(response.getBody());
            this.bearerToken = oAuthServerResponse.getAccessToken();

            // Get the token expiry time
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeProvider.currentTimeMillis());
            int seconds = oAuthServerResponse.getExpiresIn();
            calendar.add(Calendar.SECOND, seconds - 30);
            this.bearerTokenExpiry = calendar.getTimeInMillis();
            tokenRefreshes++;
            logger.atInfo()
                    .setMessage("Token expires at: {}")
                    .addArgument(calendar.getTime())
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
