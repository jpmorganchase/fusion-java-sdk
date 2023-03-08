package com.jpmorganchase.fusion.credential;

import com.jpmorganchase.fusion.http.Client;

import java.net.MalformedURLException;
import java.util.Base64;

public class OAuthSecretBasedCredentials extends OAuthCredentials{

    private final String clientSecret;

    public OAuthSecretBasedCredentials(String clientId, String clientSecret, String resource, String authServerUrl) throws MalformedURLException {
        super(clientId, resource, authServerUrl);
        this.clientSecret = clientSecret;
    }

    public OAuthSecretBasedCredentials(String clientId, String clientSecret, String resource, String authServerUrl, Client client) throws MalformedURLException {
        super(clientId, resource, authServerUrl, client);
        this.clientSecret = clientSecret;
    }

    @Override
    protected String getPostBodyContent() {
        return String.format("grant_type=client_credentials&aud=%1s", getResource());
    }

    @Override
    protected boolean requiresAuthHeader() {
        return true;
    }

    @Override
    protected String getAuthHeader() {
        String auth = getClientId() + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @Override
    public boolean useProxy() {
        return false;
    }
}
