package com.jpmorganchase.fusion.credential;

import com.jpmorganchase.fusion.http.Client;
import com.jpmorganchase.fusion.http.HttpResponse;
import com.jpmorganchase.fusion.http.JdkClient;

import java.io.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public abstract class OAuthCredentials implements Credentials {

    private final String clientId;
    private final String resource;
    private final String authServerUrl;
    private String bearerToken;
    private long bearerTokenExpiry;
    private int tokenRefreshes;

    private final Client httpClient;

    public OAuthCredentials(String clientId, String resource, String authServerUrl) {
        this.clientId = clientId;
        this.resource = resource;
        this.authServerUrl = authServerUrl;
        tokenRefreshes = 0;
        bearerTokenExpiry = 0L;
        httpClient = new JdkClient();
    }

    public OAuthCredentials(String clientId, String resource, String authServerUrl, Client client) {
        this.clientId = clientId;
        this.resource = resource;
        this.authServerUrl = authServerUrl;
        tokenRefreshes = 0;
        bearerTokenExpiry = 0L;
        httpClient = client;
    }

    @Override
    public final synchronized String getBearerToken() throws IOException {
        String auth;
        String content;

        if (bearerToken == null) {

            //Check if an obtained token has expired.
            //TODO: This should possibly be outside of concurrency Control - maybe we need an AtomicBoolean hasTokenExpired?
            if (System.currentTimeMillis() < this.bearerTokenExpiry) {
                return this.bearerToken;
            }

            Map<String, String> requestHeaders = new HashMap<>();
            if (requiresAuthHeader())
                requestHeaders.put("Authorization", getAuthHeader());
            requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
            requestHeaders.put("Accept", "application/json");

            HttpResponse<String> response = httpClient.post(authServerUrl, requestHeaders, getPostBodyContent());

            //TODO: Error Handling?
            //Get the bearer token
            OAuthServerResponse oAuthServerResponse = OAuthServerResponse.fromJson(response.getBody());
            this.bearerToken = oAuthServerResponse.getAccessToken();

            //Get the token expiry time
            Calendar calendar = Calendar.getInstance();
            int seconds = oAuthServerResponse.getExpiresIn();
            calendar.add(Calendar.SECOND, seconds - 30);
            this.bearerTokenExpiry = calendar.getTimeInMillis();
            tokenRefreshes++;
            System.out.println("Token expires at: " + calendar.getTime());
            System.out.println("Number of token refreshes: " + this.tokenRefreshes);

        }
        return bearerToken;
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
