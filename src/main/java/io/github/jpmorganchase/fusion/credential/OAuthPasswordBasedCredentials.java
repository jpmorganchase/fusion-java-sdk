package io.github.jpmorganchase.fusion.credential;

import io.github.jpmorganchase.fusion.http.Client;

public class OAuthPasswordBasedCredentials extends OAuthCredentials {

    private final String username;
    private final String password;

    public OAuthPasswordBasedCredentials(
            String clientId, String username, String password, String resource, String authServerUrl) {
        super(clientId, resource, authServerUrl);
        this.username = username;
        this.password = password;
    }

    public OAuthPasswordBasedCredentials(
            String clientId, String username, String password, String resource, String authServerUrl, Client client) {
        super(clientId, resource, authServerUrl, client);
        this.username = username;
        this.password = password;
    }

    public OAuthPasswordBasedCredentials(
            String clientId,
            String username,
            String password,
            String resource,
            String authServerUrl,
            Client client,
            TimeProvider timeProvider) {
        super(clientId, resource, authServerUrl, client, timeProvider);
        this.username = username;
        this.password = password;
    }

    public OAuthPasswordBasedCredentials(OAuthPasswordBasedConfiguration config, Client client) {
        super(config.getClientId(), config.getResource(), config.getAuthServerUrl(), client);
        this.username = config.getUsername();
        this.password = config.getPassword();
    }

    @Override
    protected String getPostBodyContent() {
        return String.format(
                "grant_type=password&resource=%1$s&client_id=%2$s&username=%3$s&password=%4$s",
                getResource(), getClientId(), this.username, this.password);
    }

    @Override
    protected boolean requiresAuthHeader() {
        return false;
    }

    @Override
    protected String getAuthHeader() {
        return null;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
