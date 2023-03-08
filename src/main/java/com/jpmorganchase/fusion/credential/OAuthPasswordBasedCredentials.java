package com.jpmorganchase.fusion.credential;

import com.jpmorganchase.fusion.http.Client;

import java.net.MalformedURLException;

public class OAuthPasswordBasedCredentials extends OAuthCredentials{

    private final String username;
    private final String password;

    public OAuthPasswordBasedCredentials(String clientId, String username, String password, String resource, String authServerUrl) throws MalformedURLException {
        super(clientId, resource, authServerUrl);
        this.username = username;
        this.password = password;
    }

    public OAuthPasswordBasedCredentials(String clientId, String username, String password, String resource, String authServerUrl, Client client) throws MalformedURLException {
        super(clientId, resource, authServerUrl, client);
        this.username = username;
        this.password = password;
    }

    @Override
    protected String getPostBodyContent() {
        return String.format("grant_type=password&resource=%1$s&client_id=%2$s&username=%3$s&password=%4$s",
                getResource(), getClientId(),
                this.username, this.password);
    }

    @Override
    protected boolean requiresAuthHeader() {
        return false;
    }

    @Override
    protected String getAuthHeader() {
        return null;
    }

    @Override
    public boolean useProxy() {
        return false;
    }
}
